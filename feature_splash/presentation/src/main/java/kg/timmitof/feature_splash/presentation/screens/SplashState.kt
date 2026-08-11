package kg.timmitof.feature_splash.presentation.screens

import kg.timmitof.core.ui.base.BaseEvent
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseState

data class SplashState(
    val isLoading: Boolean = true
): BaseState()

sealed class SplashSideEffect : BaseSideEffect.UiSideEffect()

sealed class SplashEvent : BaseEvent.UiEvent() {

    data object AnimationFinished : SplashEvent()
}