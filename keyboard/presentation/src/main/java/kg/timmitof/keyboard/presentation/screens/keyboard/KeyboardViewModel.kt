package kg.timmitof.keyboard.presentation.screens.keyboard

import androidx.lifecycle.viewModelScope
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseViewModel
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.ClipboardDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.EmojiDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.FieldContextDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.FontDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.LanguageDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.LayerDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.SettingsDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.SuggestionsDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.TextInputDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.states.ComposingText
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardLayer
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardOverlay
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
import kg.timmitof.keyboard.presentation.screens.keyboard.states.QuickSetting
import kotlinx.coroutines.flow.launchIn
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
) : BaseViewModel<KeyboardState, KeyboardSideEffect, KeyboardEvent>(KeyboardState()) {

    override fun onEvent(event: KeyboardEvent) {
        when (event) {
            is KeyboardEvent.OnKeySelect -> intent { with(textInputDelegate) { typeCharacter(event.character) } }
            is KeyboardEvent.OnSpace -> intent { with(textInputDelegate) { typeSpace() } }
            is KeyboardEvent.OnCursorMove -> intent { with(textInputDelegate) { moveCursor(event.horizontal, event.vertical) } }
            is KeyboardEvent.OnCursorModeChange -> intent { with(textInputDelegate) { setCursorMode(event.active) } }
            is KeyboardEvent.OnEnter -> intent { with(textInputDelegate) { pressEnter() } }
            is KeyboardEvent.OnSuggestionSelect -> intent { with(textInputDelegate) { applySuggestion(event.suggestion) } }
            is KeyboardEvent.OnShift -> intent { with(textInputDelegate) { toggleShift() } }
            is KeyboardEvent.OnBackspace -> intent { with(textInputDelegate) { deleteBackward() } }
            is KeyboardEvent.OnBackspaceDeleteWord -> intent { with(textInputDelegate) { deleteWordBackward() } }
            is KeyboardEvent.OnBackspaceSelectChange -> intent { with(textInputDelegate) { changeBackspaceSelection(event.chars) } }
            is KeyboardEvent.OnBackspaceSelectCommit -> intent { with(textInputDelegate) { commitBackspaceSelection(event.chars) } }
            is KeyboardEvent.OnLanguageSelect -> intent {
                with(languageDelegate) { selectLanguage(event.language) }
                with(suggestionsDelegate) { requestSuggestions() }
            }
            is KeyboardEvent.OnFontSelect -> intent { with(fontDelegate) { selectFont(event.font) } }
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
            is KeyboardEvent.OnEmojiSearchQueryChange -> intent { with(emojiDelegate) { updateSearchQuery(event.query) } }
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
            is KeyboardEvent.OnOpenApp -> intent {
                reduce { state.copy(keyboardOverlay = null) }
                postSideEffect(KeyboardSideEffect.OpenApp)
            }
            is KeyboardEvent.OnInputSessionChange -> resetInputSession()
            is KeyboardEvent.OnFieldContextChange -> intent {
                with(fieldContextDelegate) { applyContext(event.context) }
                with(suggestionsDelegate) { requestSuggestions() }
            }
            is KeyboardEvent.OnTextContextChange -> intent {
                with(textInputDelegate) { reconcileComposing(event.context) }
                with(suggestionsDelegate) { applyTextContext(event.context) }
            }
        }
    }

    override suspend fun Syntax<KeyboardState, BaseSideEffect>.onBootstrap() {
        // Настройки первыми: от них зависят и шрифт по умолчанию, и вид раскладки.
        with(settingsDelegate) { loadSettings() }
        with(languageDelegate) { loadLanguages() }
        with(fontDelegate) { loadFonts() }
        with(layerDelegate) { applyLayer(KeyboardLayer.LETTERS) }

        layerDelegate.preloadLayouts(state.languages.map { it.code })
        observeSuggestions()
        observeSettings()
        observeFontPanel()
        observeClipboard()

        val languageCode = state.activeLanguage?.code

        viewModelScope.launch {
            emojiDelegate.prefetchVariants()
            languageCode?.let { suggestionsDelegate.prefetch(it) }
        }
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

    /** Раскладка пересобирается вместе с настройками: цифровой ряд появляется сразу. */
    private fun observeSettings() {
        settingsDelegate.settings
            .onEach { settings ->
                intent {
                    with(settingsDelegate) { applySettings(settings) }
                    with(layerDelegate) { applyLayer(state.layer) }
                    with(suggestionsDelegate) { requestSuggestions() }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun prefetchSearchIndex() = viewModelScope.launch {
        emojiDelegate.prefetchSearchIndex()
    }

    private fun resetInputSession() = intent {
        with(textInputDelegate) { resetShift() }
        with(fontDelegate) { forgetFontIfNeeded() }
        clipboardDelegate.captureSystemClip()
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

        reduce {
            state.copy(
                emojiSearchQuery = "",
                emojiSearchResults = emptyList(),
                emojiSearchSelection = 0,
            )
        }
        with(layerDelegate) { applyLayer(KeyboardLayer.LETTERS) }
    }
}
