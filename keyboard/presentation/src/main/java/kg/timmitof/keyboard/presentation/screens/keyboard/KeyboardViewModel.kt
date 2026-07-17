package kg.timmitof.keyboard.presentation.screens.keyboard

import androidx.lifecycle.viewModelScope
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseViewModel
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.EmojiDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.FontDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.LanguageDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.LayerDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.TextInputDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardLayer
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.syntax.Syntax

/**
 * Orbit-синтаксис клавиатуры: даёт делегатам доступ к `state`, `reduce`
 * и `postSideEffect` — методы делегатов объявляются с этим ресивером
 * и вызываются из `intent {}` ViewModel через `with(delegate) { ... }`.
 */
internal typealias KeyboardSyntax = Syntax<KeyboardState, BaseSideEffect>

internal class KeyboardViewModel(
    private val layerDelegate: LayerDelegate,
    private val textInputDelegate: TextInputDelegate,
    private val emojiDelegate: EmojiDelegate,
    private val languageDelegate: LanguageDelegate,
    private val fontDelegate: FontDelegate,
) : BaseViewModel<KeyboardState, KeyboardSideEffect, KeyboardEvent>(KeyboardState()) {

    override fun onEvent(event: KeyboardEvent) {
        when (event) {
            is KeyboardEvent.OnKeySelect -> intent { with(textInputDelegate) { typeCharacter(event.char) } }
            is KeyboardEvent.OnSpace -> intent { with(textInputDelegate) { typeSpace() } }
            is KeyboardEvent.OnEnter -> intent { with(textInputDelegate) { pressEnter() } }
            is KeyboardEvent.OnShift -> intent { with(textInputDelegate) { toggleShift() } }
            is KeyboardEvent.OnBackspace -> intent { with(textInputDelegate) { deleteBackward() } }
            is KeyboardEvent.OnBackspaceDeleteWord -> intent { with(textInputDelegate) { deleteWordBackward() } }
            is KeyboardEvent.OnBackspaceSelectChange -> intent { with(textInputDelegate) { changeBackspaceSelection(event.chars) } }
            is KeyboardEvent.OnBackspaceSelectCommit -> intent { with(textInputDelegate) { commitBackspaceSelection(event.chars) } }
            is KeyboardEvent.OnLanguageSelect -> intent { with(languageDelegate) { selectLanguage(event.language) } }
            is KeyboardEvent.OnFontSelect -> intent { with(fontDelegate) { selectFont(event.font) } }
            is KeyboardEvent.OnSymbolsSwitch -> intent { with(layerDelegate) { applyLayer(KeyboardLayer.SYMBOLS) } }
            is KeyboardEvent.OnSymbolsAltSwitch -> intent { with(layerDelegate) { toggleSymbolsAlt() } }
            is KeyboardEvent.OnAbcSwitch -> intent { with(layerDelegate) { applyLayer(KeyboardLayer.LETTERS) } }
            is KeyboardEvent.OnEmojiSwitch -> intent { with(emojiDelegate) { openEmojiPanel() } }
            is KeyboardEvent.OnEmojiSelect -> intent { with(emojiDelegate) { selectEmoji(event.emoji) } }
            is KeyboardEvent.OnEmojiVariantSelect -> intent { with(emojiDelegate) { selectVariant(event.base, event.variant) } }
            is KeyboardEvent.OnEmojiSearchOpen -> intent { with(emojiDelegate) { openSearch() } }
            is KeyboardEvent.OnEmojiSearchClose -> intent { with(layerDelegate) { applyLayer(KeyboardLayer.EMOJI) } }
            is KeyboardEvent.OnEmojiSearchQueryChange -> intent { with(emojiDelegate) { updateSearchQuery(event.query) } }
            is KeyboardEvent.OnInputSessionChange -> resetInputSession()
            is KeyboardEvent.OnEnterActionChange -> intent { with(textInputDelegate) { changeEnterAction(event.action) } }
        }
    }

    override suspend fun Syntax<KeyboardState, BaseSideEffect>.onBootstrap() {
        with(languageDelegate) { loadLanguages() }
        with(fontDelegate) { loadFonts() }
        with(layerDelegate) { applyLayer(KeyboardLayer.LETTERS) }

        layerDelegate.preloadLayouts(state.languages.map { it.code })

        viewModelScope.launch {
            emojiDelegate.prefetchVariants()
        }
    }

    /** Смена поля ввода: сбрасываем поиск эмодзи и возвращаем буквенный слой. */
    private fun resetInputSession() = intent {
        if (state.layer == KeyboardLayer.LETTERS) return@intent

        reduce {
            state.copy(
                emojiSearchQuery = "",
                emojiSearchResults = emptyList(),
                emojiSearchSelection = 0
            )
        }
        with(layerDelegate) { applyLayer(KeyboardLayer.LETTERS) }
    }
}
