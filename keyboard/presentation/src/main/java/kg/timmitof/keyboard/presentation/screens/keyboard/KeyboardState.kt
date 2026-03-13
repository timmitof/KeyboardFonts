package kg.timmitof.keyboard.presentation.screens.keyboard

import kg.timmitof.core.ui.base.BaseEvent
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseState
import kg.timmitof.keyboard.domain.model.KeyboardKey
import kg.timmitof.keyboard.domain.model.KeyboardLayout


data class KeyboardState(
    val keyboardLayout: KeyboardLayout = KeyboardLayout("", emptyList())
): BaseState()

sealed class KeyboardSideEffect : BaseSideEffect.UiSideEffect() {

}

sealed class KeyboardEvent : BaseEvent.UiEvent() {
    data class OnKeySelect(val key: KeyboardKey) : KeyboardEvent()
}