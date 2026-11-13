package kg.timmitof.keyboard.presentation.screens.keyboard

import androidx.lifecycle.viewModelScope
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseViewModel
import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.ClipboardDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.EmojiDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.FieldContextDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.FontDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.LanguageDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.LayerDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.SettingsDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.SuggestionsDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.TextInputDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.TranslatorDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.states.ComposingText
import kg.timmitof.keyboard.presentation.screens.keyboard.states.InlineEditor
import kg.timmitof.keyboard.presentation.screens.keyboard.states.InlineTarget
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardLayer
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardOverlay
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
import kg.timmitof.keyboard.presentation.screens.keyboard.states.QuickSetting
import kg.timmitof.keyboard.presentation.screens.keyboard.states.ShiftState
import kg.timmitof.keyboard.translate.domain.model.TranslationLanguage
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.syntax.Syntax

/** Orbit-синтаксис: делегаты получают `state`, `reduce` и `postSideEffect` через этот ресивер. */
internal typealias KeyboardSyntax = Syntax<KeyboardState, BaseSideEffect>

internal class KeyboardViewModel(
    private val layerDelegate: LayerDelegate,
    private val textInputDelegate: TextInputDelegate,
    private val emojiDelegate: EmojiDelegate,
    private val languageDelegate: LanguageDelegate,
    private val fontDelegate: FontDelegate,
    private val fieldContextDelegate: FieldContextDelegate,
    private val suggestionsDelegate: SuggestionsDelegate,
    private val settingsDelegate: SettingsDelegate,
    private val clipboardDelegate: ClipboardDelegate,
    private val translatorDelegate: TranslatorDelegate,
) : BaseViewModel<KeyboardState, KeyboardSideEffect, KeyboardEvent>(KeyboardState()) {

    override fun onEvent(event: KeyboardEvent) {
        when (event) {
            is KeyboardEvent.OnKeySelect -> intent { with(textInputDelegate) { typeCharacter(event.character, event.touch) } }
            is KeyboardEvent.OnSpace -> intent { with(textInputDelegate) { typeSpace() } }
            is KeyboardEvent.OnCursorMove -> intent { with(textInputDelegate) { moveCursor(event.horizontal, event.vertical) } }
            is KeyboardEvent.OnCursorModeChange -> intent { with(textInputDelegate) { setCursorMode(event.active) } }
            is KeyboardEvent.OnEnter -> intent {
                if (state.inlineTarget == InlineTarget.LANGUAGE_SEARCH) {
                    with(textInputDelegate) { closeComposing() }
                    with(translatorDelegate) { selectFirstFound() }
                } else if (state.translator != null && state.layer != KeyboardLayer.EMOJI_SEARCH) {
                    with(textInputDelegate) { closeComposing() }
                    with(translatorDelegate) { send() }
                } else {
                    with(textInputDelegate) { pressEnter() }
                }
            }
            is KeyboardEvent.OnSuggestionSelect -> intent { with(textInputDelegate) { applySuggestion(event.suggestion) } }
            is KeyboardEvent.OnShift -> intent { with(textInputDelegate) { toggleShift() } }
            is KeyboardEvent.OnBackspace -> intent { with(textInputDelegate) { deleteBackward() } }
            is KeyboardEvent.OnBackspaceDeleteWord -> intent { with(textInputDelegate) { deleteWordBackward() } }
            is KeyboardEvent.OnBackspaceSelectChange -> intent { with(textInputDelegate) { changeBackspaceSelection(event.chars) } }
            is KeyboardEvent.OnBackspaceSelectCommit -> intent { with(textInputDelegate) { commitBackspaceSelection() } }
            is KeyboardEvent.OnLanguageSelect -> intent {
                with(languageDelegate) { selectLanguage(event.language) }
                with(translatorDelegate) { syncSource() }
                with(suggestionsDelegate) { requestSuggestions() }
            }
            is KeyboardEvent.OnFontSelect -> intent {
                with(fontDelegate) { selectFont(event.font) }
                // В панели перевода шрифт — для перевода: перерисовываем уже готовый.
                with(translatorDelegate) { restyle() }
            }
            is KeyboardEvent.OnFontsExpandedChange -> intent { with(fontDelegate) { setFontsExpanded(event.expanded) } }
            is KeyboardEvent.OnSymbolsSwitch -> intent { with(layerDelegate) { applyLayer(KeyboardLayer.SYMBOLS) } }
            is KeyboardEvent.OnSymbolsAltSwitch -> intent { with(layerDelegate) { toggleSymbolsAlt() } }
            is KeyboardEvent.OnAbcSwitch -> intent {
                with(layerDelegate) { applyLayer(KeyboardLayer.LETTERS) }
                with(suggestionsDelegate) { requestSuggestions() }
            }
            is KeyboardEvent.OnEmojiSwitch -> {
                intent {
                    with(textInputDelegate) { closeComposing() }
                    with(emojiDelegate) { openEmojiPanel() }
                    with(suggestionsDelegate) { requestSuggestions() }
                }
                prefetchSearchIndex()
            }
            is KeyboardEvent.OnEmojiSelect -> intent {
                with(textInputDelegate) { closeComposing() }
                with(emojiDelegate) { selectEmoji(event.emoji) }
            }
            is KeyboardEvent.OnEmojiVariantSelect -> intent {
                with(textInputDelegate) { closeComposing() }
                with(emojiDelegate) { selectVariant(event.base, event.variant) }
            }
            is KeyboardEvent.OnEmojiSearchOpen -> intent { with(emojiDelegate) { openSearch() } }
            is KeyboardEvent.OnEmojiSearchClose -> intent {
                with(layerDelegate) { applyLayer(KeyboardLayer.EMOJI) }
                with(suggestionsDelegate) { requestSuggestions() }
            }
            is KeyboardEvent.OnOverlayChange -> openOverlay(event.overlay)
            is KeyboardEvent.OnQuickSetting -> intent {
                when (val setting = event.setting) {
                    is QuickSetting.Toggle -> settingsDelegate.setToggle(setting.toggle, setting.isEnabled)
                    is QuickSetting.Height -> settingsDelegate.setHeight(setting.height)
                    is QuickSetting.Theme -> with(settingsDelegate) { setTheme(setting.theme) }
                }
            }
            is KeyboardEvent.OnClipboardPaste -> intent {
                with(textInputDelegate) { closeComposing() }
                with(clipboardDelegate) { paste(event.entry.text) }
            }
            is KeyboardEvent.OnClipboardAction -> intent { clipboardDelegate.applyAction(event.action) }
            is KeyboardEvent.OnHideKeyboard -> intent { postSideEffect(KeyboardSideEffect.HideKeyboard) }
            is KeyboardEvent.OnOpenApp -> intent {
                reduce { state.copy(keyboardOverlay = null) }
                postSideEffect(KeyboardSideEffect.OpenApp)
            }
            is KeyboardEvent.OnTranslatorToggle -> toggleTranslator()
            is KeyboardEvent.OnTranslatorSwap -> swapTranslator()
            is KeyboardEvent.OnTranslatorFontsToggle -> intent {
                val isOpen = state.translator?.isFontPickerOpen ?: return@intent
                with(translatorDelegate) { setFontPickerOpen(!isOpen) }
            }
            is KeyboardEvent.OnTranslatorClear -> intent { with(translatorDelegate) { clear() } }
            is KeyboardEvent.OnTranslatorRetry -> intent { with(translatorDelegate) { retry() } }
            is KeyboardEvent.OnTranslatorSearchOpen -> intent {
                // Черновик панели закрываем: дальше клавиши пишут в запрос поиска.
                with(textInputDelegate) { closeComposing() }
                with(translatorDelegate) { openSearch() }
            }
            is KeyboardEvent.OnTranslatorSearchClose -> intent {
                with(textInputDelegate) { closeComposing() }
                with(translatorDelegate) { closeSearch() }
            }
            is KeyboardEvent.OnTranslatorAutoSourceSelect -> intent { with(translatorDelegate) { selectAutoSource() } }
            is KeyboardEvent.OnTranslatorDetectionDismiss -> intent { with(translatorDelegate) { dismissDetection() } }
            is KeyboardEvent.OnTranslatorLanguageDownload -> intent {
                with(translatorDelegate) { downloadLanguage(event.language) }
            }
            is KeyboardEvent.OnTranslatorSourceSelect -> selectTranslatorSource(event.language)
            is KeyboardEvent.OnTranslatorDownload -> intent { with(translatorDelegate) { download() } }
            is KeyboardEvent.OnTranslatorTargetSelect -> intent { with(translatorDelegate) { selectTarget(event.language) } }
            is KeyboardEvent.OnInputSessionChange -> resetInputSession()
            is KeyboardEvent.OnInputSessionFinish -> {
                viewModelScope.launch { suggestionsDelegate.flush() }
                intent { with(translatorDelegate) { dismiss() } }
            }
            is KeyboardEvent.OnFieldContextChange -> intent {
                with(fieldContextDelegate) { applyContext(event.context) }
                closeTranslatorIfNotAllowed()
                with(suggestionsDelegate) {
                    refreshAutoShift()
                    requestSuggestions()
                }
            }
            is KeyboardEvent.OnTextContextChange -> intent {
                // Пока открыт переводчик, снимок текста — панельный; поле приложения держит только перевод.
                if (state.translator != null) return@intent
                with(textInputDelegate) { reconcileComposing(event.context) }
                with(suggestionsDelegate) { applyTextContext(event.context) }
            }
        }
    }

    override suspend fun Syntax<KeyboardState, BaseSideEffect>.onBootstrap() {
        // Настройки первыми: от них зависят и шрифт по умолчанию, и вид раскладки.
        with(settingsDelegate) { loadSettings() }
        with(languageDelegate) { loadLanguages() }
        with(translatorDelegate) { loadHint() }

        // Прогрев словаря и эмодзи — параллельно с остальной загрузкой, первый ввод не ждёт.
        prefetchDictionary(state.activeLanguage)
        viewModelScope.launch { emojiDelegate.prefetchVariants() }

        with(fontDelegate) { loadFonts() }
        with(layerDelegate) { applyLayer(KeyboardLayer.LETTERS) }

        layerDelegate.preloadLayouts(state.languages.map { it.layout })
        observeSuggestions()
        observeSettings()
        observeFeatureAccess()
        observeLanguages()
        observeFontPanel()
        observeClipboard()
        observeTranslator()
        observeEmojiSearch()
    }

    /** Модель переводчика держит десятки мегабайт — с клавиатурой её и отпускаем. */
    override fun onCleared() {
        translatorDelegate.release()
        super.onCleared()
    }

    /** Запрос правит набор (`input`), а результаты пересчитываются по готовому запросу. */
    private fun observeEmojiSearch() {
        container.stateFlow
            .map { it.emojiSearch.text.takeIf { _ -> it.layer == KeyboardLayer.EMOJI_SEARCH } }
            .distinctUntilChanged()
            .filterNotNull()
            .onEach { query -> intent { with(emojiDelegate) { search(query) } } }
            .launchIn(viewModelScope)
    }

    private fun toggleTranslator() = intent {
        if (state.translator != null) {
            closeTranslator()
            return@intent
        }
        // Черновик закрываем в поле приложения: дальше ввод пойдёт в панель.
        with(textInputDelegate) { closeComposing() }
        with(translatorDelegate) { open() }
    }

    /** Переводчик выключили в настройках (или он перестал быть доступен) — открытая панель закрывается сразу. */
    private fun observeFeatureAccess() {
        settingsDelegate.featureAccess
            .onEach { access ->
                intent {
                    with(settingsDelegate) { applyFeatureAccess(access) }
                    closeTranslatorIfNotAllowed()
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun KeyboardSyntax.closeTranslatorIfNotAllowed() {
        if (state.translator != null && !state.allowsTranslation) closeTranslator()
    }

    private suspend fun KeyboardSyntax.closeTranslator() {
        with(textInputDelegate) { closeComposing() }
        with(translatorDelegate) { close() }
    }

    /** Исходный язык перевода — это раскладка: выбор в листе переключает её, а пара подстраивается сама. */
    private fun selectTranslatorSource(language: TranslationLanguage) = intent {
        val layout = state.translator?.layouts?.get(language.code) ?: return@intent

        with(textInputDelegate) { closeComposing() }
        with(languageDelegate) { selectLanguage(layout) }
        with(translatorDelegate) {
            disableAutoSource()
            syncSource()
        }
        reduce { state.copy(keyboardOverlay = null) }
        prefetchDictionary(state.activeLanguage)
        with(suggestionsDelegate) { requestSuggestions() }
    }

    /** Местами меняем, только если на целевом языке можно печатать: раскладка следует исходному языку. */
    private fun swapTranslator() = intent {
        val translator = state.translator ?: return@intent
        val layout = translator.swapLayout ?: return@intent
        val previousSource = translator.source ?: return@intent

        with(textInputDelegate) { closeComposing() }
        with(languageDelegate) { selectLanguage(layout) }
        with(translatorDelegate) { swapped(previousSource) }
        prefetchDictionary(state.activeLanguage)
    }

    /**
     * Поле панели ведёт себя как поле приложения: после правки его снимок становится снимком текста
     * (подсказки, авто-Shift), а через паузу текст уходит переводчику.
     */
    private fun observeTranslator() {
        container.stateFlow
            .map { it.translator?.editor?.text }
            .distinctUntilChanged()
            .filterNotNull()
            .onEach {
                intent {
                    val context = state.translator?.editor?.textContext ?: return@intent
                    with(textInputDelegate) { reconcileComposing(context) }
                    with(suggestionsDelegate) { applyTextContext(context) }
                }
            }
            .launchIn(viewModelScope)

        translatorDelegate.models
            .onEach { models -> intent { with(translatorDelegate) { applyModels(models) } } }
            .launchIn(viewModelScope)

        translatorDelegate.detections(container.stateFlow.map { it.translator?.detectionRequest(it.activeLanguage?.code) })
            .onEach { (request, detected) ->
                intent { with(translatorDelegate) { applyDetection(request, detected) } }
            }
            .launchIn(viewModelScope)

        translatorDelegate.translations(container.stateFlow.map { it.translator?.request })
            .onEach { (request, result) ->
                intent { with(translatorDelegate) { applyTranslation(request, result) } }
            }
            .launchIn(viewModelScope)
    }

    private fun openOverlay(overlay: KeyboardOverlay?) = intent {
        if (overlay == KeyboardOverlay.CLIPBOARD) clipboardDelegate.captureSystemClip()

        reduce { state.copy(keyboardOverlay = overlay) }
    }

    private fun observeClipboard() {
        clipboardDelegate.board
            .onEach { board -> intent { with(clipboardDelegate) { applyBoard(board) } } }
            .launchIn(viewModelScope)
    }

    private fun observeFontPanel() {
        fontDelegate.panel
            .onEach { panel -> intent { with(fontDelegate) { applyPanel(panel) } } }
            .launchIn(viewModelScope)
    }

    private fun observeSuggestions() {
        suggestionsDelegate.suggestions
            .onEach { (request, suggestions) ->
                intent { with(suggestionsDelegate) { applySuggestions(request, suggestions) } }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Раскладку и подсказки пересобираем только если изменилось влияющее на них поле:
     * громкость, цвет или фон на них не влияют. Первая эмиссия совпадает с загруженными настройками и ничего не делает.
     */
    private fun observeSettings() {
        settingsDelegate.settings
            .onEach { settings ->
                intent {
                    val previous = state.settings
                    with(settingsDelegate) { applySettings(settings) }
                    closeTranslatorIfNotAllowed()

                    if (previous.isDigitsRowEnabled != settings.isDigitsRowEnabled) {
                        with(layerDelegate) { applyLayer(state.layer) }
                    }
                    if (previous.affectsSuggestions != settings.affectsSuggestions) {
                        with(suggestionsDelegate) { requestSuggestions() }
                    }
                    if (previous.isAutoCapitalizeEnabled != settings.isAutoCapitalizeEnabled) {
                        if (settings.isAutoCapitalizeEnabled) {
                            with(suggestionsDelegate) { refreshAutoShift() }
                        } else if (state.shiftState == ShiftState.ACTIVE) {
                            // Выключили — поставленная автоматически заглавная больше не нужна.
                            with(textInputDelegate) { resetShift() }
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Включённые языки и их порядок меняют в настройках. Первая эмиссия совпадает с загруженными и ничего не делает;
     * сменился язык ввода (удалили выбранный) — пересобираем раскладку и подсказки.
     */
    private fun observeLanguages() {
        languageDelegate.languages
            .onEach { languages ->
                intent {
                    val isLanguageChanged = with(languageDelegate) { applyLanguages(languages) }
                    with(translatorDelegate) { syncSource() }
                    if (isLanguageChanged) {
                        prefetchDictionary(state.activeLanguage)
                        with(suggestionsDelegate) { requestSuggestions() }
                    }
                }
                layerDelegate.preloadLayouts(languages.enabled.map { it.layout })
            }
            .launchIn(viewModelScope)
    }

    /** Прогрев словаря — в фоне, первый ввод не ждёт. Язык без словаря прогревать нечего. */
    private fun prefetchDictionary(language: KeyboardLanguage?) {
        val code = language?.takeIf(KeyboardLanguage::hasDictionary)?.code ?: return
        viewModelScope.launch { suggestionsDelegate.prefetch(code) }
    }

    private val KeyboardSettings.affectsSuggestions: List<Boolean>
        get() = listOf(isSuggestionsEnabled, isNextWordPredictionEnabled, isAutoCorrectEnabled)

    private fun prefetchSearchIndex() = viewModelScope.launch {
        emojiDelegate.prefetchSearchIndex()
    }

    private fun resetInputSession() = intent {
        // Shift прошлой сессии (и Caps Lock) не переносим, а заглавную для пустого поля или начала предложения
        // ставим сразу: снимок текста мог не измениться, и сам он её не включит.
        // Переводчик привязан к прошлому полю, его черновик система закрыла сама.
        if (state.translator != null) reduce { state.copy(translator = null) }
        with(textInputDelegate) { resetShift() }
        with(suggestionsDelegate) { refreshAutoShift() }
        with(fontDelegate) { forgetFontIfNeeded() }
        reduce {
            state.copy(
                suggestions = emptyList(),
                suggestionsWord = "",
                autoCorrection = null,
                composing = ComposingText(),
                isFontsExpanded = state.allowsFonts,
                keyboardOverlay = null,
            )
        }

        if (state.layer == KeyboardLayer.LETTERS) return@intent

        reduce { state.copy(emojiSearch = InlineEditor(), emojiSearchResults = emptyList()) }
        with(layerDelegate) { applyLayer(KeyboardLayer.LETTERS) }
    }
}
