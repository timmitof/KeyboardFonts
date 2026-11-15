package kg.timmitof.core.billing.data.source

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

class PurchasesResponse(
    val result: BillingResult,
    val purchases: List<Purchase>,
)

@Singleton
class PlayBillingClient @Inject constructor(
    @ApplicationContext context: Context,
) {

    private val responses = MutableSharedFlow<PurchasesResponse>(extraBufferCapacity = BUFFER)

    val purchasesResponses: SharedFlow<PurchasesResponse> = responses.asSharedFlow()

    private val client = BillingClient.newBuilder(context)
        .setListener { result, purchases ->
            responses.tryEmit(PurchasesResponse(result, purchases.orEmpty()))
        }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    val isReady: Boolean get() = client.isReady

    suspend fun startConnection(onDisconnected: () -> Unit): BillingResult =
        suspendCancellableCoroutine { continuation ->
            val settled = AtomicBoolean(false)
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (settled.compareAndSet(false, true)) continuation.resume(result)
                }

                override fun onBillingServiceDisconnected() {
                    if (settled.compareAndSet(false, true)) {
                        continuation.resume(result(BillingResponseCode.SERVICE_DISCONNECTED))
                    } else {
                        onDisconnected()
                    }
                }
            })
        }

    suspend fun queryProductDetails(ids: List<String>): Pair<BillingResult, List<ProductDetails>> {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                ids.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(ProductType.INAPP)
                        .build()
                }
            )
            .build()
        return suspendCancellableCoroutine { continuation ->
            client.queryProductDetailsAsync(params) { result, details ->
                continuation.resume(result to details.productDetailsList)
            }
        }
    }

    suspend fun queryPurchases(): Pair<BillingResult, List<Purchase>> {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(ProductType.INAPP)
            .build()
        return suspendCancellableCoroutine { continuation ->
            client.queryPurchasesAsync(params) { result, purchases ->
                continuation.resume(result to purchases)
            }
        }
    }

    /** Заодно подтверждает покупку (acknowledge). */
    suspend fun consume(purchaseToken: String): BillingResult {
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
        return suspendCancellableCoroutine { continuation ->
            client.consumeAsync(params) { result, _ -> continuation.resume(result) }
        }
    }

    /** Только с главного потока. */
    fun launchBillingFlow(activity: Activity, product: ProductDetails): BillingResult {
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(product).build())
            )
            .build()
        return client.launchBillingFlow(activity, params)
    }

    private fun result(code: Int): BillingResult = BillingResult.newBuilder().setResponseCode(code).build()

    private companion object {
        const val BUFFER = 8
    }
}
