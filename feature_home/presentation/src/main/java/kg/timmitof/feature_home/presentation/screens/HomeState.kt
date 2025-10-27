package kg.timmitof.feature_home.presentation.screens

import kg.timmitof.core.ui.base.BaseEvent
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseState

/**
 * `HomeState` represents the current UI state of the **Home** screen.
 */
data class HomeState(
    val background: List<String> = emptyList()
): BaseState()

/** Screen-specific one-off effects */
sealed class HomeSideEffect : BaseSideEffect.UiSideEffect() {

}

/** All actions coming from the UI */
sealed class HomeEvent : BaseEvent.UiEvent() {
    data class BackgroundSelected(val background: String) : HomeEvent()
}