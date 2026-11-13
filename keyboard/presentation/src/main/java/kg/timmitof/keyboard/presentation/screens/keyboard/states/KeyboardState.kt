package kg.timmitof.keyboard.presentation.screens.keyboard.states

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import kg.timmitof.core.ui.base.BaseEvent
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseState
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardBoard
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardEntry
import kg.timmitof.keyboard.domain.model.EmojiCategory
import kg.timmitof.keyboard.domain.model.FeatureAccess
import kg.timmitof.keyboard.domain.model.KeyboardFeature
import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyCharacter
import kg.timmitof.keyboard.font.domain.model.FontScript
import kg.timmitof.keyboard.font.domain.model.KeyboardFont
import kg.timmitof.keyboard.presentation.components.fontScript
import kg.timmitof.keyboard.presentation.components.orDefaultFor
import kg.timmitof.keyboard.presentation.components.supporting
import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.domain.model.KeyboardLayout
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kg.timmitof.keyboard.suggestion.domain.model.TextContext
import kg.timmitof.keyboard.suggestion.domain.model.TypedKey
import kg.timmitof.keyboard.suggestion.domain.model.WordSuggestion
import kg.timmitof.keyboard.translate.domain.model.TranslationLanguage

@Stable
internal data class KeyboardState(
    val layer: KeyboardLayer = KeyboardLayer.LETTERS,
    val keyboardLayout: KeyboardLayout = KeyboardLayout(name = "", rows = emptyList()),
    val languages: List<KeyboardLanguage> = emptyList(),
    val selectedLanguage: KeyboardLanguage? = null,
    val fieldLanguage: KeyboardLanguage? = null,
    /** Подмена для почты и пароля: латинский язык из включённых, иначе из каталога. */
    val latinLanguage: KeyboardLanguage? = null,
    val fonts: List<KeyboardFont> = emptyList(),
    val selectedFont: KeyboardFont = KeyboardFont.Default,
    val isFontsExpanded: Boolean = true,
    val emojiCategories: List<EmojiCategory> = emptyList(),
    val emojiVariants: Map<String, List<String>> = emptyMap(),
    val preferredEmojiVariants: Map<String, String> = emptyMap(),
    val recentEmojis: List<String> = emptyList(),
    /** Запрос поиска эмодзи — поле внутри клавиатуры, как у панели перевода. */
    val emojiSearch: InlineEditor = InlineEditor(),
    val emojiSearchResults: List<String> = emptyList(),
    val shiftState: ShiftState = ShiftState.DISABLED,
    val fieldContext: KeyboardFieldContext = KeyboardFieldContext(),
    val textContext: TextContext = TextContext(),
    val suggestions: List<WordSuggestion> = emptyList(),
    val suggestionsWord: String = "",
    val autoCorrection: AutoCorrection? = null,
    val composing: ComposingText = ComposingText(),
    /** Слово с разделителем, оставленное без автозамены: Backspace сразу после него — промах Т9 (статистика). */
    val keptWord: String? = null,
    /** Последнюю букву подставили умные зоны: Backspace сразу за ней — ошибка подмены (статистика). */
    val isKeyAdapted: Boolean = false,
    val isCursorMode: Boolean = false,
    val settings: KeyboardSettings = KeyboardSettings(),
    val keyboardOverlay: KeyboardOverlay? = null,
    val clipboard: ClipboardBoard = ClipboardBoard(),
    /** Что открыто пользователю: платные фичи без покупки не работают, даже если их включили. */
    val featureAccess: FeatureAccess = FeatureAccess(),
    /** Перевод только что скопированного — карточка над клавишами; null — предлагать нечего. */
    val clipTranslation: ClipTranslation? = null,
    /** «Новое: переводчик» в верхней строке — пока переводчик ни разу не открывали. */
    val isTranslatorHintPending: Boolean = false,
    /** Открытая панель перевода; null — ввод идёт прямо в поле приложения. */
    val translator: TranslatorState? = null,
): BaseState() {

    val fieldType: KeyboardFieldType get() = fieldContext.type

    /** Куда идут клавиши; null — в поле приложения. Поиск эмодзи открывается поверх панели перевода и главнее её. */
    val inlineTarget: InlineTarget?
        get() = when {
            layer == KeyboardLayer.EMOJI_SEARCH -> InlineTarget.EMOJI_SEARCH
            translator?.languageSearch != null -> InlineTarget.LANGUAGE_SEARCH
            translator != null -> InlineTarget.TRANSLATOR
            else -> null
        }

    /** Правка поля внутри клавиатуры; ввод в поле приложения состояние не меняет. */
    fun editInline(effect: KeyboardSideEffect.Input): KeyboardState = when (inlineTarget) {
        InlineTarget.EMOJI_SEARCH -> copy(emojiSearch = emojiSearch.apply(effect))
        InlineTarget.LANGUAGE_SEARCH -> translator?.let { translator ->
            copy(translator = translator.copy(languageSearch = translator.languageSearch?.apply(effect)))
        } ?: this
        InlineTarget.TRANSLATOR -> translator?.let { copy(translator = it.copy(editor = it.editor.apply(effect))) } ?: this
        null -> this
    }

    /** Выбор пользователя главнее автоподмены на латиницу (адрес, пароль): при смене языка [fieldLanguage] сбрасывается. */
    val activeLanguage: KeyboardLanguage? get() = fieldLanguage ?: selectedLanguage

    fun latinLanguageFor(type: KeyboardFieldType): KeyboardLanguage? {
        if (!type.requiresLatinLayout || selectedLanguage?.isLatin != false) return null

        return latinLanguage
    }

    val enterAction: EnterAction get() = fieldContext.enterAction

    val displayedEnterAction: EnterAction
        get() = when {
            layer == KeyboardLayer.EMOJI_SEARCH -> EnterAction.DONE
            // В поиске языка Enter выбирает первый найденный, в панели перевода — отправляет перевод.
            inlineTarget == InlineTarget.LANGUAGE_SEARCH -> EnterAction.DONE
            inlineTarget == InlineTarget.TRANSLATOR -> EnterAction.SEND
            fieldType.hasOwnLayout && enterAction == EnterAction.RETURN -> EnterAction.DONE
            else -> enterAction
        }

    /**
     * Панель убирают и настройки, и поле: в адресах, паролях и цифрах стилизация мешает.
     * В режиме перевода набираемый текст не стилизуем — переводчик его не прочтёт; шрифт получает перевод.
     */
    val allowsFonts: Boolean
        get() = allowsStyledText && translator == null

    private val allowsStyledText: Boolean
        get() = settings.isFontsPanelEnabled && fieldType.allowsFonts

    /**
     * Переводчик включён (и открыт пользователю) и поле подходит: в паролях, адресах и цифрах,
     * как и со стилизацией, переводить нечего.
     */
    val allowsTranslation: Boolean
        get() = fieldType.allowsFonts && settings.isActive(KeyboardFeature.TRANSLATOR, featureAccess)

    /** Кнопка «Aa» панели перевода: шрифт можно применить к переводу. */
    val allowsTranslationFont: Boolean get() = allowsStyledText

    /** Шрифт перевода — выбранный на панели «Aa», на алфавите целевого языка, а не раскладки. */
    fun translationFont(target: TranslationLanguage): KeyboardFont =
        if (allowsTranslationFont) selectedFont.orDefaultFor(target.fontScript) else KeyboardFont.Default

    /** Запрос определения языка набранного в панели; null — определять незачем. */
    val translatorDetectionRequest: DetectionRequest?
        get() = translator?.detectionRequest(activeLanguage, languages)

    /** Подпись пробела в режиме перевода: «Русский → English». */
    val translatorPairLabel: String?
        get() = translator?.let {
            "${it.translationSource?.name ?: activeLanguage?.displayName.orEmpty()} → ${it.target.name}"
        }

    /** Алфавит раскладки: стили без его поддержки прячутся с панели и не применяются. */
    val fontScript: FontScript get() = activeLanguage.fontScript

    val availableFonts: List<KeyboardFont> get() = fonts.supporting(fontScript)

    val activeFont: KeyboardFont
        // В поиск эмодзи и панель перевода — обычные буквы: стилизованный запрос не найдёт и не переведётся.
        get() = if (allowsFonts && inlineTarget == null) selectedFont.orDefaultFor(fontScript) else KeyboardFont.Default

    /** Язык без словаря Т9 работает без подсказок, автозамены и обучения. */
    val allowsSuggestions: Boolean
        get() = settings.isSuggestionsEnabled && fieldType.allowsSuggestions && layer.showsSuggestions &&
                activeLanguage?.hasDictionary == true && inlineTarget?.isSearch != true

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

/** @param touches касания по буквам черновика, одно на букву: по ним Т9 ищет промахи пальцем. */
@Stable
internal data class ComposingText(
    val text: String = "",
    val hasCorrection: Boolean = false,
    val touches: List<TypedKey> = emptyList(),
) {
    val isActive: Boolean get() = text.isNotEmpty()
}

/**
 * Замена, которую Backspace сразу после неё возвращает назад: автозамена слова или точка двойным пробелом.
 *
 * @param rejection что разучить при отмене; у точки его нет.
 */
@Stable
internal data class AutoCorrection(
    val original: String,
    val corrected: String,
    val rejection: CorrectionRejection? = null,
)

/** Данные автозамены слова, нужные, чтобы её отмена разучила выученное. */
@Stable
internal data class CorrectionRejection(
    val previousWord: String,
    val typed: String,
    val corrected: String,
    val wasLearned: Boolean,
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

        /** Перечитать поле: клавиатура долго писала мимо снимка (панель перевода) и он устарел. */
        data object RefreshTextContext : Input()
    }

    data object OpenApp : KeyboardSideEffect()

    data object HideKeyboard : KeyboardSideEffect()
}

internal sealed class KeyboardEvent : BaseEvent.UiEvent() {
    data class OnKeySelect(val character: KeyCharacter, val touch: Offset = Offset.Unspecified) : KeyboardEvent()
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
    data class OnOverlayChange(val overlay: KeyboardOverlay?) : KeyboardEvent()
    data class OnQuickSetting(val setting: QuickSetting) : KeyboardEvent()
    data class OnClipboardPaste(val entry: ClipboardEntry) : KeyboardEvent()
    data class OnClipboardAction(val action: ClipboardAction) : KeyboardEvent()
    data object OnOpenApp : KeyboardEvent()
    data object OnTranslatorToggle : KeyboardEvent()
    data object OnTranslatorSwap : KeyboardEvent()
    data object OnTranslatorFontsToggle : KeyboardEvent()
    data object OnTranslatorClear : KeyboardEvent()
    data object OnTranslatorRetry : KeyboardEvent()
    data object OnTranslatorSearchOpen : KeyboardEvent()
    data object OnTranslatorSearchClose : KeyboardEvent()
    data class OnTranslatorLanguageDownload(val language: TranslationLanguage) : KeyboardEvent()
    data object OnTranslatorAutoSourceSelect : KeyboardEvent()
    data object OnClipTranslationPaste : KeyboardEvent()
    data object OnClipTranslationReply : KeyboardEvent()
    data object OnClipTranslationDismiss : KeyboardEvent()
    data object OnTranslatorDetectionDismiss : KeyboardEvent()
    data class OnTranslatorSourceSelect(val language: TranslationLanguage) : KeyboardEvent()
    data object OnTranslatorDownload : KeyboardEvent()
    data class OnTranslatorTargetSelect(val language: TranslationLanguage) : KeyboardEvent()
    data object OnHideKeyboard : KeyboardEvent()
}
