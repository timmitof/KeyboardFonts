package kg.timmitof.keyboard.presentation.screens.keyboard.states

import androidx.compose.runtime.Stable
import kg.timmitof.core.ui.base.BaseEvent
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseState
import kg.timmitof.keyboard.domain.model.EmojiCategory
import kg.timmitof.keyboard.domain.model.KeyboardFont
import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.domain.model.KeyboardLayout

@Stable
internal data class KeyboardState(
    val layer: KeyboardLayer = KeyboardLayer.LETTERS,
    val keyboardLayout: KeyboardLayout = KeyboardLayout("", emptyList()),
    val languages: List<KeyboardLanguage> = emptyList(),
    val selectedLanguage: KeyboardLanguage? = null,
    val fonts: List<KeyboardFont> = emptyList(),
    val selectedFont: KeyboardFont = KeyboardFont.Default,
    val emojiCategories: List<EmojiCategory> = emptyList(),
    val emojiVariants: Map<String, List<String>> = emptyMap(),
    val preferredEmojiVariants: Map<String, String> = emptyMap(),
    val recentEmojis: List<String> = emptyList(),
    val emojiSearchQuery: String = "",
    val emojiSearchResults: List<String> = emptyList(),
    val emojiSearchSelection: Int = 0,
    val shiftState: ShiftState = ShiftState.DISABLED,
    val enterAction: EnterAction = EnterAction.RETURN,
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
    data class OnEmojiVariantSelect(val base: String, val variant: String) : KeyboardEvent()
    data object OnInputSessionChange : KeyboardEvent()
    data class OnEnterActionChange(val action: EnterAction) : KeyboardEvent()
    data object OnShift : KeyboardEvent()
    data object OnBackspace : KeyboardEvent()
    data object OnBackspaceDeleteWord : KeyboardEvent()
    data class OnBackspaceSelectChange(val chars: Int) : KeyboardEvent()
    data class OnBackspaceSelectCommit(val chars: Int) : KeyboardEvent()
    data object OnSpace : KeyboardEvent()
    data object OnEnter : KeyboardEvent()
    data class OnLanguageSelect(val language: KeyboardLanguage) : KeyboardEvent()
    data class OnFontSelect(val font: KeyboardFont) : KeyboardEvent()
    data object OnSymbolsSwitch : KeyboardEvent()
    data object OnSymbolsAltSwitch : KeyboardEvent()
    data object OnAbcSwitch : KeyboardEvent()
    data object OnEmojiSwitch : KeyboardEvent()
    data object OnEmojiSearchOpen : KeyboardEvent()
    data object OnEmojiSearchClose : KeyboardEvent()
    data class OnEmojiSearchQueryChange(val query: String) : KeyboardEvent()
}