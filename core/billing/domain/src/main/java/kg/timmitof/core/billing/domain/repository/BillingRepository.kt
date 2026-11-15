package kg.timmitof.core.billing.domain.repository

import android.app.Activity
import kg.timmitof.core.billing.domain.model.BillingOutcome
import kg.timmitof.core.billing.domain.model.BillingProduct
import kg.timmitof.core.billing.domain.model.BillingStatus
import kg.timmitof.core.billing.domain.model.PurchaseUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Работает только в процессе приложения: окну оплаты нужна активити.
 * Обрабатываются только товары из каталога (`Set<BillingProductSpec>` в графе Hilt).
 */
interface BillingRepository {

    val status: StateFlow<BillingStatus>

    /** Горячий поток без повтора: подписываться до [launchPurchase]. */
    val purchaseUpdates: Flow<PurchaseUpdate>

    fun observePurchasedProducts(): Flow<Set<String>>

    suspend fun connect(): BillingStatus

    suspend fun products(ids: List<String>): BillingOutcome<List<BillingProduct>>

    /** Исход придёт в [purchaseUpdates]; здесь — только удалось ли открыть окно оплаты. */
    suspend fun launchPurchase(activity: Activity, productId: String): BillingOutcome<Unit>

    /** Оплаченные, но не обработанные (приложение закрылось) и отложенные, которые с тех пор оплатили. */
    suspend fun completePendingPurchases()
}
