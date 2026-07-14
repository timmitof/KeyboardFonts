package kg.timmitof.keyboard.presentation.screens.keyboard

import androidx.compose.runtime.Stable
import kg.timmitof.core.ui.base.BaseEvent
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseState
import kg.timmitof.keyboard.domain.model.KeyboardKey
import kg.timmitof.keyboard.domain.model.KeyboardLayout

@Stable
internal data class KeyboardState(
    val keyboardLayout: KeyboardLayout = KeyboardLayout("", emptyList()),
    val isUpperCase: Boolean = false,
    val isCapsLock: Boolean = false,
): BaseState()

sealed class KeyboardSideEffect : BaseSideEffect.UiSideEffect() {
    data class CommitText(val char: CharSequence) : KeyboardSideEffect()
    data object DeleteBackward : KeyboardSideEffect()
    data object PerformEditorAction : KeyboardSideEffect()
}

internal sealed class KeyboardEvent : BaseEvent.UiEvent() {
    data class OnKeySelect(val char: String) : KeyboardEvent()
    data object OnShift : KeyboardEvent()
    data object OnBackspace : KeyboardEvent()
    data object OnSpace : KeyboardEvent()
    data object OnEnter : KeyboardEvent()
    data object OnSymbolsSwitch : KeyboardEvent()
    data object OnEmojiSwitch : KeyboardEvent()
}