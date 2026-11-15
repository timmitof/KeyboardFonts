package kg.timmitof.feature_settings.data.interactor

import android.app.Activity
import kg.timmitof.core.billing.domain.model.BillingOutcome
import kg.timmitof.core.billing.domain.model.PurchaseUpdate
import kg.timmitof.core.billing.domain.repository.BillingRepository
import kg.timmitof.feature_settings.domain.interactor.TipJarInteractor
import kg.timmitof.feature_settings.domain.model.Tip
import kg.timmitof.feature_settings.domain.model.TipOffer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import javax.inject.Inject

class TipJarInteractorImpl @Inject constructor(
    private val billingRepository: BillingRepository,
) : TipJarInteractor {

    // Исходы без товаров (отмена, ошибка) относятся к открытому окну оплаты; с товарами — только к чаевым.
    override val purchaseUpdates: Flow<PurchaseUpdate> = billingRepository.purchaseUpdates.filter { update ->
        when (update) {
            is PurchaseUpdate.Completed -> update.productIds.any { Tip.byProductId(it) != null }
            is PurchaseUpdate.Pending -> update.productIds.any { Tip.byProductId(it) != null }
            PurchaseUpdate.Cancelled, is PurchaseUpdate.Failed -> true
        }
    }

    override suspend fun loadTips(): BillingOutcome<List<TipOffer>> =
        when (val outcome = billingRepository.products(Tip.entries.map { it.productId })) {
            is BillingOutcome.Failure -> outcome
            is BillingOutcome.Success -> BillingOutcome.Success(
                outcome.value.mapNotNull { product ->
                    Tip.byProductId(product.id)?.let { TipOffer(it, product.formattedPrice) }
                }
            )
        }

    override suspend fun buy(activity: Activity, tip: Tip): BillingOutcome<Unit> =
        billingRepository.launchPurchase(activity, tip.productId)

    override suspend fun completePending() {
        billingRepository.completePendingPurchases()
    }
}
