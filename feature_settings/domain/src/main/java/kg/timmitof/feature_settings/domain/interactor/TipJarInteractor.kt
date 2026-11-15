package kg.timmitof.feature_settings.domain.interactor

import android.app.Activity
import kg.timmitof.core.billing.domain.model.BillingOutcome
import kg.timmitof.core.billing.domain.model.PurchaseUpdate
import kg.timmitof.feature_settings.domain.model.Tip
import kg.timmitof.feature_settings.domain.model.TipOffer
import kotlinx.coroutines.flow.Flow

/** «Банка для чаевых»: уровни с ценами из Play и оплата. Чаевые ничего не открывают — только благодарность. */
interface TipJarInteractor {

    /** Исходы оплат чаевых; подписываться до [buy]. */
    val purchaseUpdates: Flow<PurchaseUpdate>

    /** Уровни в порядке [Tip], только те, что есть в Play. */
    suspend fun loadTips(): BillingOutcome<List<TipOffer>>

    /** Открывает окно оплаты; исход — в [purchaseUpdates]. */
    suspend fun buy(activity: Activity, tip: Tip): BillingOutcome<Unit>

    /** Доводит оплаты, которые не успели обработать (приложение закрылось), и отложенные. */
    suspend fun completePending()
}
