package kg.timmitof.keyboard.presentation.screens.keyboard

import androidx.compose.runtime.Stable
import kg.timmitof.core.ui.base.BaseEvent
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseState
import kg.timmitof.keyboard.domain.model.EmojiCategory
import kg.timmitof.keyboard.domain.model.KeyboardLayout

internal enum class KeyboardLayer {
    LETTERS,
    SYMBOLS,
    SYMBOLS_ALT,
    EMOJI,
    EMOJI_SEARCH,
}

@Stable
internal data class KeyboardState(
    val layer: KeyboardLayer = KeyboardLayer.LETTERS,
    val keyboardLayout: KeyboardLayout = KeyboardLayout("", emptyList()),
    val emojiCategories: List<EmojiCategory> = emptyList(),
    val recentEmojis: List<String> = emptyList(),
    val emojiSearchQuery: String = "",
    val emojiSearchResults: List<String> = emptyList(),
    val emojiSearchSelection: Int = 0,
    val isUpperCase: Boolean = false,
    val isCapsLock: Boolean = false,
): BaseState()

sealed class KeyboardSideEffect : BaseSideEffect.UiSideEffect() {
    data class CommitText(val char: CharSequence) : KeyboardSideEffect()
    data class SelectBeforeCursor(val chars: Int) : KeyboardSideEffect()
    data object DeleteBackward : KeyboardSideEffect()
    data object PerformEditorAction : KeyboardSideEffect()
    data object DeleteWordBackward : KeyboardSideEffect()
    data object DeleteSelection : KeyboardSideEffect()
}

internal sealed class KeyboardEvent : BaseEvent.UiEvent() {
    data class OnKeySelect(val char: String) : KeyboardEvent()
    data class OnEmojiSelect(val emoji: String) : KeyboardEvent()
    data object OnShift : KeyboardEvent()
    data object OnBackspace : KeyboardEvent()
    data object OnBackspaceDeleteWord : KeyboardEvent()
    data class OnBackspaceSelectChange(val chars: Int) : KeyboardEvent()
    data class OnBackspaceSelectCommit(val chars: Int) : KeyboardEvent()
    data object OnSpace : KeyboardEvent()
    data object OnEnter : KeyboardEvent()
    data object OnSymbolsSwitch : KeyboardEvent()
    data object OnSymbolsAltSwitch : KeyboardEvent()
    data object OnAbcSwitch : KeyboardEvent()
    data object OnEmojiSwitch : KeyboardEvent()
    data object OnEmojiSearchOpen : KeyboardEvent()
    data object OnEmojiSearchClose : KeyboardEvent()
    data class OnEmojiSearchQueryChange(val query: String) : KeyboardEvent()
}