package kg.timmitof.feature_splash.presentation.screens

import kg.timmitof.core.ui.base.BaseEvent
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseState

/**
 * `SplashState` represents the current UI state of the **Splash** screen.
 */
data class SplashState(
    val isLoading: Boolean = true
): BaseState()

/** Screen-specific one-off effects */
sealed class SplashSideEffect : BaseSideEffect.UiSideEffect()

/** All actions coming from the UI */
sealed class SplashEvent : BaseEvent.UiEvent()