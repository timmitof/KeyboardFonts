package kg.timmitof.feature_settings.presentation.screens.tipjar

import android.app.Activity
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.timmitof.core.billing.domain.model.BillingFailure
import kg.timmitof.core.billing.domain.model.BillingOutcome
import kg.timmitof.core.billing.domain.model.PurchaseUpdate
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseViewModel
import kg.timmitof.feature_settings.domain.interactor.TipJarInteractor
import kg.timmitof.feature_settings.domain.model.Tip
import org.orbitmvi.orbit.syntax.Syntax
import javax.inject.Inject

@HiltViewModel
class TipJarViewModel @Inject constructor(
    private val tipJarInteractor: TipJarInteractor,
) : BaseViewModel<TipJarState, TipJarSideEffect, TipJarEvent>(TipJarState()) {

    init {
        observePurchases()
    }

    override fun onEvent(event: TipJarEvent) {
        when (event) {
            is TipJarEvent.Shown -> onShown()
            is TipJarEvent.TipSelected -> intent {
                if (!state.isPaying) reduce { state.copy(selected = event.tip, paymentFailure = null) }
            }
            is TipJarEvent.PayClicked -> pay(event.activity)
            is TipJarEvent.RetryClicked -> loadTips()
            is TipJarEvent.CloseClicked -> navigateBack()
        }
    }

    override suspend fun Syntax<TipJarState, BaseSideEffect>.onBootstrap() {
        loadTips()
    }

    private fun onShown() = intent {
        when (state.phase) {
            is TipJarPhase.Thanks -> reduce { state.copy(phase = TipJarPhase.Picking, paymentFailure = null) }
            is TipJarPhase.Failed -> loadTips()
            TipJarPhase.Loading, TipJarPhase.Picking -> reduce { state.copy(paymentFailure = null) }
        }
        tipJarInteractor.completePending()
    }

    private fun loadTips() = intent {
        reduce { state.copy(phase = TipJarPhase.Loading) }
        when (val outcome = tipJarInteractor.loadTips()) {
            is BillingOutcome.Success if outcome.value.isEmpty() -> reduce {
                state.copy(phase = TipJarPhase.Failed(BillingFailure.PRODUCT_UNAVAILABLE))
            }
            is BillingOutcome.Success -> reduce {
                val tips = outcome.value
                // Выбранного уровня может не быть в Play — тогда средний из доступных.
                val selected = state.selected.takeIf { tip -> tips.any { it.tip == tip } } ?: tips[tips.size / 2].tip
                state.copy(phase = TipJarPhase.Picking, tips = tips, selected = selected)
            }
            is BillingOutcome.Failure -> reduce { state.copy(phase = TipJarPhase.Failed(outcome.failure)) }
        }
    }

    private fun pay(activity: Activity) = intent {
        if (state.isPaying || state.phase != TipJarPhase.Picking) return@intent
        reduce { state.copy(isPaying = true, paymentFailure = null) }
        val outcome = tipJarInteractor.buy(activity, state.selected)
        if (outcome is BillingOutcome.Failure) {
            reduce { state.copy(isPaying = false, paymentFailure = outcome.failure) }
        }
        // Исход придёт в observePurchases.
    }

    private fun observePurchases() = intent {
        tipJarInteractor.purchaseUpdates.collect { update ->
            when (update) {
                is PurchaseUpdate.Completed -> {
                    val tip = update.productIds.firstNotNullOfOrNull(Tip::byProductId) ?: state.selected
                    reduce {
                        state.copy(phase = TipJarPhase.Thanks(tip), isPaying = false, isPending = false, paymentFailure = null)
                    }
                    postSideEffect(TipJarSideEffect.Thanked)
                }
                is PurchaseUpdate.Pending -> reduce { state.copy(isPaying = false, isPending = true) }
                PurchaseUpdate.Cancelled -> reduce { state.copy(isPaying = false) }
                is PurchaseUpdate.Failed -> reduce { state.copy(isPaying = false, paymentFailure = update.failure) }
            }
        }
    }
}
