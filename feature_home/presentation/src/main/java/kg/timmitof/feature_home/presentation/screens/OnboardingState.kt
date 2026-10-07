package kg.timmitof.feature_home.presentation.screens

import androidx.compose.runtime.Immutable
import kg.timmitof.core.ui.base.BaseEvent
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseState
import kg.timmitof.feature_home.domain.model.KeyboardSetupModel
import kg.timmitof.feature_home.domain.model.KeyboardSetupStep

@Immutable
data class OnboardingState(
    val setup: KeyboardSetupModel = KeyboardSetupModel(),
) : BaseState()

sealed class OnboardingSideEffect : BaseSideEffect.UiSideEffect()

sealed class OnboardingEvent : BaseEvent.UiEvent() {

    data object ScreenResumed : OnboardingEvent()

    data class StepActionClicked(val step: KeyboardSetupStep) : OnboardingEvent()

    data object DoneClicked : OnboardingEvent()
}
