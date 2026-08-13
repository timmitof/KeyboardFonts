package kg.timmitof.keyboard.presentation.screens.keyboard.states

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import kg.timmitof.core.ui.base.BaseEvent
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseState
import kg.timmitof.keyboard.domain.model.EmojiCategory
import kg.timmitof.keyboard.domain.model.KeyCharacter
import kg.timmitof.keyboard.domain.model.KeyboardFont
import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.domain.model.KeyboardLayout
import kg.timmitof.keyboard.domain.model.TextContext
import kg.timmitof.keyboard.domain.model.WordSuggestion
import kg.timmitof.keyboard.presentation.R

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
    val isCursorMode: Boolean = false,
): BaseState() {

    val fieldType: KeyboardFieldType get() = fieldContext.type

    /**
     * Раскладка, которой набирают прямо сейчас.
     *
     * В адресе почты и пароле клавиатура сама встаёт на латиницу, но выбор
     * пользователя главнее: как только он сменит язык руками, [fieldLanguage]
     * сбрасывается и снова действует [selectedLanguage].
     */
    val activeLanguage: KeyboardLanguage? get() = fieldLanguage ?: selectedLanguage

    /**
     * Латинская раскладка, которой поле [type] нужно открыть вместо выбранной.
     *
     * `null` — подмена не нужна: поле принимает любой алфавит либо пользователь
     * и так набирает латиницей.
     */
    fun latinLanguageFor(type: KeyboardFieldType): KeyboardLanguage? {
        if (!type.requiresLatinLayout || selectedLanguage?.isLatin != false) return null

        return languages.firstOrNull(KeyboardLanguage::isLatin)
    }

    val enterAction: EnterAction get() = fieldContext.enterAction

    /**
     * Действие на клавише Enter.
     */
    val displayedEnterAction: EnterAction
        get() = when {
            layer == KeyboardLayer.EMOJI_SEARCH -> EnterAction.DONE
            fieldType.hasOwnLayout && enterAction == EnterAction.RETURN -> EnterAction.DONE
            else -> enterAction
        }

    /** Шрифт, который реально применяется: в адресах, паролях и цифрах ввод остаётся обычным. */
    val activeFont: KeyboardFont
        get() = if (fieldType.allowsFonts) selectedFont else KeyboardFont.Default

    /** Работает ли Т9 прямо сейчас: и поле, и слой должны это позволять. */
    val allowsSuggestions: Boolean
        get() = fieldType.allowsSuggestions && layer.showsSuggestions

    /** Показывать ли подсказки слов вместо шрифтов. */
    val hasSuggestions: Boolean
        get() = suggestions.isNotEmpty() && allowsSuggestions

    /** Подсказка, которой пробел заменит набранное слово (если исправление нашлось). */
    val pendingAutoCorrect: WordSuggestion?
        get() = suggestions.firstOrNull { it.isAutoCorrect }.takeIf { allowsSuggestions }

    /**
     * Посчитаны ли подсказки именно для того слова, которое сейчас набрано.
     *
     * Расчёт идёт в фоне с небольшой паузой, и при быстром наборе пробел легко
     * обгоняет его — тогда автозамену нужно досчитать на месте, а не пропускать.
     */
    val hasFreshSuggestions: Boolean
        get() = suggestionsWord == textContext.composingWord

    /** Плашка-пояснение в верхней панели: почему клавиатура ведёт себя иначе. */
    @get:StringRes
    val noticeRes: Int?
        get() = fieldType.noticeRes
            ?: R.string.field_notice_multiline.takeIf { fieldContext.isMultiLine }

    /**
     * Вариант нижнего ряда для текущего поля.
     *
     * Тип поля важнее действия Enter: в адресе нужен `@`, даже если поле просит «Найти».
     */
    val bottomRowVariant: String?
        get() = fieldType.bottomRowVariant ?: when (enterAction) {
            EnterAction.SEARCH -> "search"
            EnterAction.SEND -> "message"
            else -> null
        }
}

/**
 * Автозамена, которую применил пробел.
 */
@Stable
internal data class AutoCorrection(
    val original: String,
    val corrected: String,
)

sealed class KeyboardSideEffect : BaseSideEffect.UiSideEffect() {
    data class CommitText(val char: CharSequence) : KeyboardSideEffect()
    data class SelectBeforeCursor(val chars: Int) : KeyboardSideEffect()
    data class MoveCursor(val horizontal: Int, val vertical: Int) : KeyboardSideEffect()
    data class ReplaceWordBeforeCursor(val text: CharSequence) : KeyboardSideEffect()
    data class ReplaceTextBeforeCursor(val chars: Int, val text: CharSequence) : KeyboardSideEffect()
    data object DeleteBackward : KeyboardSideEffect()
    data object PerformEditorAction : KeyboardSideEffect()
    data object DeleteWordBackward : KeyboardSideEffect()
    data object DeleteSelection : KeyboardSideEffect()
}

internal sealed class KeyboardEvent : BaseEvent.UiEvent() {
    data class OnKeySelect(val character: KeyCharacter) : KeyboardEvent()
    data class OnEmojiSelect(val emoji: String) : KeyboardEvent()
    data class OnEmojiVariantSelect(val base: String, val variant: String) : KeyboardEvent()
    data object OnInputSessionChange : KeyboardEvent()
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
}
