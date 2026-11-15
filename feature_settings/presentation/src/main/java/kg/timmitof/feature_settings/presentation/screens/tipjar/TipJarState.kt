package kg.timmitof.feature_settings.presentation.screens.tipjar

import android.app.Activity
import androidx.compose.runtime.Immutable
import kg.timmitof.core.billing.domain.model.BillingFailure
import kg.timmitof.core.ui.base.BaseEvent
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseState
import kg.timmitof.feature_settings.domain.model.Tip
import kg.timmitof.feature_settings.domain.model.TipOffer

@Immutable
sealed interface TipJarPhase {
    data object Loading : TipJarPhase

    data object Picking : TipJarPhase

    data class Thanks(val tip: Tip) : TipJarPhase

    data class Failed(val failure: BillingFailure) : TipJarPhase
}

@Immutable
data class TipJarState(
    val phase: TipJarPhase = TipJarPhase.Loading,
    val tips: List<TipOffer> = emptyList(),
    val selected: Tip = Tip.PIZZA,
    val isPaying: Boolean = false,
    val isPending: Boolean = false,
    val paymentFailure: BillingFailure? = null,
) : BaseState() {

    val selectedOffer: TipOffer? get() = tips.firstOrNull { it.tip == selected }
}

sealed class TipJarSideEffect : BaseSideEffect.UiSideEffect() {
    data object Thanked : TipJarSideEffect()
}

sealed class TipJarEvent : BaseEvent.UiEvent() {

    /** ViewModel живёт на экране «Студии», дольше шторки, — при каждом открытии сбрасываем благодарность. */
    data object Shown : TipJarEvent()

    data class TipSelected(val tip: Tip) : TipJarEvent()

    /** Активити дальше вызова не хранится. */
    data class PayClicked(val activity: Activity) : TipJarEvent()

    data object RetryClicked : TipJarEvent()

    data object CloseClicked : TipJarEvent()
}
