package kg.timmitof.feature_settings.domain.interactor

import android.app.Activity
import kg.timmitof.core.billing.domain.model.BillingOutcome
import kg.timmitof.core.billing.domain.model.PurchaseUpdate
import kg.timmitof.feature_settings.domain.model.Tip
import kg.timmitof.feature_settings.domain.model.TipOffer
import kotlinx.coroutines.flow.Flow

interface TipJarInteractor {

    /** Подписываться до [buy]. */
    val purchaseUpdates: Flow<PurchaseUpdate>

    fun observeSupporter(): Flow<Boolean>

    /** false — только если Play прямо сказал «недоступно»; при сбоях сети пункт не прячем. */
    suspend fun isAvailable(): Boolean

    suspend fun loadTips(): BillingOutcome<List<TipOffer>>

    suspend fun buy(activity: Activity, tip: Tip): BillingOutcome<Unit>

    suspend fun completePending()
}
