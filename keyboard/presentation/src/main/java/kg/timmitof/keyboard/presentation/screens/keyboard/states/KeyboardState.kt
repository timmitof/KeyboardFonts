package kg.timmitof.keyboard.presentation.screens.keyboard.states

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import kg.timmitof.core.ui.base.BaseEvent
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseState
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardBoard
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardEntry
import kg.timmitof.keyboard.domain.model.EmojiCategory
import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyCharacter
import kg.timmitof.keyboard.font.domain.model.KeyboardFont
import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.domain.model.KeyboardLayout
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kg.timmitof.keyboard.suggestion.domain.model.TextContext
import kg.timmitof.keyboard.suggestion.domain.model.WordSuggestion

@Stable
internal data class KeyboardState(
    val layer: KeyboardLayer = KeyboardLayer.LETTERS,
    val keyboardLayout: KeyboardLayout = KeyboardLayout(name = "", rows = emptyList()),
    val languages: List<KeyboardLanguage> = emptyList(),
    val selectedLanguage: KeyboardLanguage? = null,
    val fieldLanguage: KeyboardLanguage? = null,
    val fonts: List<KeyboardFont> = emptyList(),
    val selectedFont: KeyboardFont = KeyboardFont.Default,
    val isFontsExpanded: Boolean = true,
    val emojiCategories: List<EmojiCategory> = emptyList(),
    val emojiVariants: Map<String, List<String>> = emptyMap(),
    val preferredEmojiVariants: Map<String, String> = emptyMap(),
    val recentEmojis: List<String> = emptyList(),
    val emojiSearchQuery: String = "",
    val emojiSearchResults: List<String> = emptyList(),
    val emojiSearchSelection: Int = 0,
    val shiftState: ShiftState = ShiftState.DISABLED,
    val fieldContext: KeyboardFieldContext = KeyboardFieldContext(),
    val textContext: TextContext = TextContext(),
    val suggestions: List<WordSuggestion> = emptyList(),
    val suggestionsWord: String = "",
    val autoCorrection: AutoCorrection? = null,
    val composing: ComposingText = ComposingText(),
    val isCursorMode: Boolean = false,
    val settings: KeyboardSettings = KeyboardSettings(),
    val keyboardOverlay: KeyboardOverlay? = null,
    val clipboard: ClipboardBoard = ClipboardBoard(),
): BaseState() {

    val fieldType: KeyboardFieldType get() = fieldContext.type

    /** Выбор пользователя главнее автоподмены на латиницу (адрес, пароль): при смене языка [fieldLanguage] сбрасывается. */
    val activeLanguage: KeyboardLanguage? get() = fieldLanguage ?: selectedLanguage

    fun latinLanguageFor(type: KeyboardFieldType): KeyboardLanguage? {
        if (!type.requiresLatinLayout || selectedLanguage?.isLatin != false) return null

        return languages.firstOrNull(KeyboardLanguage::isLatin)
    }

    val enterAction: EnterAction get() = fieldContext.enterAction

    val displayedEnterAction: EnterAction
        get() = when {
            layer == KeyboardLayer.EMOJI_SEARCH -> EnterAction.DONE
            fieldType.hasOwnLayout && enterAction == EnterAction.RETURN -> EnterAction.DONE
            else -> enterAction
        }

    /** Панель убирают и настройки, и поле: в адресах, паролях и цифрах стилизация мешает. */
    val allowsFonts: Boolean
        get() = settings.isFontsPanelEnabled && fieldType.allowsFonts

    val activeFont: KeyboardFont
        get() = if (allowsFonts) selectedFont else KeyboardFont.Default

    val allowsSuggestions: Boolean
        get() = settings.isSuggestionsEnabled && fieldType.allowsSuggestions && layer.showsSuggestions

    val hasSuggestions: Boolean
        get() = suggestions.isNotEmpty() && allowsSuggestions

    val pendingAutoCorrect: WordSuggestion?
        get() = suggestions.firstOrNull { it.isAutoCorrect }.takeIf { allowsSuggestions }

    val allowsComposing: Boolean get() = allowsSuggestions

    val canStartComposing: Boolean
        get() = allowsComposing && (composing.isActive || textContext.composingWord.isEmpty())

    /** Расчёт идёт в фоне, и пробел может его обогнать — тогда автозамену досчитываем на месте. */
    val hasFreshSuggestions: Boolean
        get() = suggestionsWord == textContext.composingWord

    @get:StringRes
    val noticeRes: Int?
        get() = fieldType.noticeRes

    /** Тип поля важнее Enter: в адресе нужен `@`, даже если поле просит «Найти». */
    val bottomRowVariant: String?
        get() = fieldType.bottomRowVariant ?: when (enterAction) {
            EnterAction.SEARCH -> "search"
            EnterAction.SEND -> "message"
            else -> null
        }
}

internal sealed class QuickSetting {
    data class Toggle(val toggle: KeyboardToggle, val isEnabled: Boolean) : QuickSetting()
    data class Height(val height: KeyboardHeight) : QuickSetting()
    data class Theme(val theme: KeyboardThemeMode) : QuickSetting()
}

internal sealed class ClipboardAction {
    data class Pin(val entry: ClipboardEntry, val isPinned: Boolean) : ClipboardAction()
    data class Remove(val entry: ClipboardEntry) : ClipboardAction()
    data object ClearRecent : ClipboardAction()
}

@Stable
internal data class ComposingText(
    val text: String = "",
    val hasCorrection: Boolean = false,
) {
    val isActive: Boolean get() = text.isNotEmpty()
}

@Stable
internal data class AutoCorrection(
    val original: String,
    val corrected: String,
)

sealed class KeyboardSideEffect : BaseSideEffect.UiSideEffect() {

    sealed class Input : KeyboardSideEffect() {
        data class CommitText(val char: CharSequence) : Input()

        data class SetComposingText(
            val text: CharSequence,
            val hasCorrection: Boolean = false,
        ) : Input()

        data object FinishComposing : Input()
        data class SelectBeforeCursor(val chars: Int) : Input()
        data class MoveCursor(val horizontal: Int, val vertical: Int) : Input()
        data class ReplaceWordBeforeCursor(val text: CharSequence) : Input()
        data class ReplaceTextBeforeCursor(val chars: Int, val text: CharSequence) : Input()
        data object DeleteBackward : Input()
        data object PerformEditorAction : Input()
        data object DeleteWordBackward : Input()
        data object DeleteSelection : Input()
    }

    data object OpenApp : KeyboardSideEffect()

    data object HideKeyboard : KeyboardSideEffect()
}

internal sealed class KeyboardEvent : BaseEvent.UiEvent() {
    data class OnKeySelect(val character: KeyCharacter) : KeyboardEvent()
    data class OnEmojiSelect(val emoji: String) : KeyboardEvent()
    data class OnEmojiVariantSelect(val base: String, val variant: String) : KeyboardEvent()
    data object OnInputSessionChange : KeyboardEvent()
    data object OnInputSessionFinish : KeyboardEvent()
    data class OnFieldContextChange(val context: KeyboardFieldContext) : KeyboardEvent()
    data class OnTextContextChange(val context: TextContext) : KeyboardEvent()
    data object OnShift : KeyboardEvent()
    data object OnBackspace : KeyboardEvent()
    data object OnBackspaceDeleteWord : KeyboardEvent()
    data class OnBackspaceSelectChange(val chars: Int) : KeyboardEvent()
    data class OnBackspaceSelectCommit(val chars: Int) : KeyboardEvent()
    data object OnSpace : KeyboardEvent()
    data class OnCursorMove(val horizontal: Int, val vertical: Int) : KeyboardEvent()
    data class OnCursorModeChange(val active: Boolean) : KeyboardEvent()
    data object OnEnter : KeyboardEvent()
    data class OnLanguageSelect(val language: KeyboardLanguage) : KeyboardEvent()
    data class OnFontSelect(val font: KeyboardFont) : KeyboardEvent()
    data class OnFontsExpandedChange(val expanded: Boolean) : KeyboardEvent()
    data class OnSuggestionSelect(val suggestion: WordSuggestion) : KeyboardEvent()
    data object OnSymbolsSwitch : KeyboardEvent()
    data object OnSymbolsAltSwitch : KeyboardEvent()
    data object OnAbcSwitch : KeyboardEvent()
    data object OnEmojiSwitch : KeyboardEvent()
    data object OnEmojiSearchOpen : KeyboardEvent()
    data object OnEmojiSearchClose : KeyboardEvent()
    data class OnEmojiSearchQueryChange(val query: String) : KeyboardEvent()
    data class OnOverlayChange(val overlay: KeyboardOverlay?) : KeyboardEvent()
    data class OnQuickSetting(val setting: QuickSetting) : KeyboardEvent()
    data class OnClipboardPaste(val entry: ClipboardEntry) : KeyboardEvent()
    data class OnClipboardAction(val action: ClipboardAction) : KeyboardEvent()
    data object OnOpenApp : KeyboardEvent()
    data object OnHideKeyboard : KeyboardEvent()
}
