package kg.timmitof.core.billing.domain.repository

import android.app.Activity
import kg.timmitof.core.billing.domain.model.BillingOutcome
import kg.timmitof.core.billing.domain.model.BillingProduct
import kg.timmitof.core.billing.domain.model.BillingStatus
import kg.timmitof.core.billing.domain.model.PurchaseUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Google Play Billing. Работает только в процессе приложения: окну оплаты нужна активити, у клавиатуры её нет.
 * Обрабатываются только товары из каталога (`Set<BillingProductSpec>` в графе Hilt).
 */
interface BillingRepository {

    val status: StateFlow<BillingStatus>

    /**
     * Все исходы покупок — и из окна оплаты, и доведённые [completePendingPurchases].
     * Горячий поток без повтора: подписываться заранее, до [launchPurchase].
     */
    val purchaseUpdates: Flow<PurchaseUpdate>

    /** Подключается (с повторами при сбоях) и ждёт итога. После ошибки повторный вызов пробует снова. */
    suspend fun connect(): BillingStatus

    /** Товары с ценами из Play в порядке [ids]; товары, которых Play не знает, пропускаются. */
    suspend fun products(ids: List<String>): BillingOutcome<List<BillingProduct>>

    /** Открывает окно оплаты. Исход придёт в [purchaseUpdates]; здесь — только удалось ли его открыть. */
    suspend fun launchPurchase(activity: Activity, productId: String): BillingOutcome<Unit>

    /**
     * Доводит «висящие» покупки: оплачены, но не обработаны (приложение закрылось раньше)
     * или отложенные, которые с тех пор оплатили. Вызывать при открытии экрана.
     */
    suspend fun completePendingPurchases()
}
