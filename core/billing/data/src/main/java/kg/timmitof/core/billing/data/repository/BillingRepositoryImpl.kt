package kg.timmitof.core.billing.data.repository

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import kg.timmitof.core.billing.data.mapper.isOk
import kg.timmitof.core.billing.data.mapper.isRetryable
import kg.timmitof.core.billing.data.mapper.toBillingProduct
import kg.timmitof.core.billing.data.mapper.toFailure
import kg.timmitof.core.billing.data.source.PlayBillingClient
import kg.timmitof.core.billing.data.source.PurchasesResponse
import kg.timmitof.core.billing.domain.model.BillingFailure
import kg.timmitof.core.billing.domain.model.BillingOutcome
import kg.timmitof.core.billing.domain.model.BillingProduct
import kg.timmitof.core.billing.domain.model.BillingProductKind
import kg.timmitof.core.billing.domain.model.BillingProductSpec
import kg.timmitof.core.billing.domain.model.BillingStatus
import kg.timmitof.core.billing.domain.model.PurchaseUpdate
import kg.timmitof.core.billing.domain.repository.BillingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Покупки через Play. Все оплаты — из окна оплаты и «висящие» — проходят через [process]:
 * оплачено → употребить → [PurchaseUpdate.Completed]; отложено → [PurchaseUpdate.Pending], ничего не трогаем.
 * Серверной проверки подписи нет сознательно: товары ничего не открывают, подделывать их бессмысленно.
 */
@Singleton
class BillingRepositoryImpl @Inject constructor(
    private val client: PlayBillingClient,
    catalog: Set<@JvmSuppressWildcards BillingProductSpec>,
) : BillingRepository {

    private val specs = catalog.associateBy { it.id }

    // Колбэки Play приходят на главный поток — там же и обрабатываем, без лишних переключений.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _status = MutableStateFlow<BillingStatus>(BillingStatus.Idle)
    override val status: StateFlow<BillingStatus> = _status.asStateFlow()

    private val updates = MutableSharedFlow<PurchaseUpdate>(extraBufferCapacity = UPDATES_BUFFER)
    override val purchaseUpdates: Flow<PurchaseUpdate> = updates.asSharedFlow()

    private val connectMutex = Mutex()
    private val processMutex = Mutex()
    private var reconnectJob: Job? = null

    /** Описания товаров из последнего запроса: без них окно оплаты не открыть. */
    private val details = ConcurrentHashMap<String, ProductDetails>()

    /** Оплаты, за которые уже поблагодарили, — чтобы окно оплаты и проверка при открытии не сказали «спасибо» дважды. */
    private val thankedTokens = mutableSetOf<String>()

    init {
        scope.launch {
            client.purchasesResponses.collect(::onPurchasesResponse)
        }
    }

    override suspend fun connect(): BillingStatus = connectMutex.withLock {
        if (client.isReady) {
            _status.value = BillingStatus.Ready
            return@withLock BillingStatus.Ready
        }
        var backoff = INITIAL_BACKOFF_MS
        var failure = BillingFailure.ERROR
        for (attempt in 1..MAX_ATTEMPTS) {
            _status.value = BillingStatus.Connecting
            val result = client.startConnection(onDisconnected = ::onServiceDisconnected)
            if (result.isOk) {
                _status.value = BillingStatus.Ready
                return@withLock BillingStatus.Ready
            }
            log("подключение", result)
            failure = result.toFailure()
            if (!result.isRetryable || attempt == MAX_ATTEMPTS) break
            delay(backoff)
            backoff = (backoff * 2).coerceAtMost(MAX_BACKOFF_MS)
        }
        BillingStatus.Failed(failure).also { _status.value = it }
    }

    override suspend fun products(ids: List<String>): BillingOutcome<List<BillingProduct>> {
        awaitReady()?.let { return BillingOutcome.Failure(it) }

        val (result, list) = client.queryProductDetails(ids)
        if (!result.isOk) {
            log("товары", result)
            return BillingOutcome.Failure(result.toFailure())
        }
        list.forEach { details[it.productId] = it }

        val byId = list.associateBy { it.productId }
        val products = ids.mapNotNull { id -> byId[id]?.toBillingProduct() }
        // Пустой ответ при OK — товары не созданы/не активны в консоли или сборка не из Play.
        return if (products.isEmpty()) {
            BillingOutcome.Failure(BillingFailure.PRODUCT_UNAVAILABLE)
        } else {
            BillingOutcome.Success(products)
        }
    }

    override suspend fun launchPurchase(activity: Activity, productId: String): BillingOutcome<Unit> {
        awaitReady()?.let { return BillingOutcome.Failure(it) }

        val product = details[productId]
            ?: products(listOf(productId)).let { details[productId] }
            ?: return BillingOutcome.Failure(BillingFailure.PRODUCT_UNAVAILABLE)

        val result = withContext(Dispatchers.Main.immediate) { client.launchBillingFlow(activity, product) }
        return when (result.responseCode) {
            BillingResponseCode.OK -> BillingOutcome.Success(Unit)
            // Прошлая оплата этого товара не употреблена — доводим её, исход придёт потоком.
            BillingResponseCode.ITEM_ALREADY_OWNED -> {
                scope.launch { completeOwned() }
                BillingOutcome.Success(Unit)
            }
            else -> {
                log("окно оплаты", result)
                if (result.responseCode == BillingResponseCode.SERVICE_DISCONNECTED) onServiceDisconnected()
                BillingOutcome.Failure(result.toFailure())
            }
        }
    }

    override suspend fun completePendingPurchases() {
        sweep()
    }

    private suspend fun onPurchasesResponse(response: PurchasesResponse) {
        val result = response.result
        when (result.responseCode) {
            BillingResponseCode.OK -> process(response.purchases)
            BillingResponseCode.USER_CANCELED -> updates.emit(PurchaseUpdate.Cancelled)
            BillingResponseCode.ITEM_ALREADY_OWNED -> completeOwned()
            BillingResponseCode.SERVICE_DISCONNECTED -> {
                log("оплата", result)
                updates.emit(PurchaseUpdate.Failed(BillingFailure.OFFLINE))
                onServiceDisconnected()
            }
            else -> {
                log("оплата", result)
                updates.emit(PurchaseUpdate.Failed(result.toFailure()))
            }
        }
    }

    /** «Уже куплено»: доводим неупотреблённую оплату; если довести нечего — честно сообщаем об ошибке. */
    private suspend fun completeOwned() {
        if (sweep() == 0) updates.emit(PurchaseUpdate.Failed(BillingFailure.ERROR))
    }

    /** @return сколько своих покупок нашлось и обработано. */
    private suspend fun sweep(): Int {
        if (awaitReady() != null) return 0
        val (result, purchases) = client.queryPurchases()
        if (!result.isOk) {
            log("незавершённые покупки", result)
            return 0
        }
        return process(purchases)
    }

    private suspend fun process(purchases: List<Purchase>): Int = processMutex.withLock {
        var handled = 0
        for (purchase in purchases) {
            // Чужие и нерасходуемые товары не трогаем: употребить их — значит отнять у пользователя покупку.
            val consumable = purchase.products.all { specs[it]?.kind == BillingProductKind.CONSUMABLE }
            if (!consumable) continue

            when (purchase.purchaseState) {
                Purchase.PurchaseState.PURCHASED -> {
                    handled++
                    consume(purchase)
                }
                Purchase.PurchaseState.PENDING -> {
                    handled++
                    updates.emit(PurchaseUpdate.Pending(purchase.products))
                }
                else -> Unit
            }
        }
        handled
    }

    /**
     * Употребление заодно подтверждает покупку (иначе Play вернёт деньги через 3 дня).
     * Благодарим и при сбое: деньги уже списаны, а употребить получится при следующем открытии экрана.
     */
    private suspend fun consume(purchase: Purchase) {
        val result = client.consume(purchase.purchaseToken)
        if (!result.isOk && result.responseCode != BillingResponseCode.ITEM_NOT_OWNED) log("употребление", result)
        if (thankedTokens.add(purchase.purchaseToken)) {
            updates.emit(PurchaseUpdate.Completed(purchase.products))
        }
    }

    /** null — готово; иначе — почему подключиться не вышло. */
    private suspend fun awaitReady(): BillingFailure? =
        (connect() as? BillingStatus.Failed)?.failure

    /** Связь с Play оборвалась после подключения — переподключаемся в фоне (с паузами, см. [connect]). */
    private fun onServiceDisconnected() {
        _status.value = BillingStatus.Idle
        if (reconnectJob?.isActive == true) return
        reconnectJob = scope.launch { connect() }
    }

    private fun log(action: String, result: BillingResult) {
        Log.w(TAG, "$action: код ${result.responseCode}, ${result.debugMessage}")
    }

    private companion object {
        const val TAG = "Billing"
        const val MAX_ATTEMPTS = 4
        const val INITIAL_BACKOFF_MS = 1_000L
        const val MAX_BACKOFF_MS = 8_000L
        const val UPDATES_BUFFER = 16
    }
}
