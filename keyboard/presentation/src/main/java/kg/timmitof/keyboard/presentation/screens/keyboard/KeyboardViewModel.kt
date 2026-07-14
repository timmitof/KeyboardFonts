package kg.timmitof.keyboard.presentation.screens.keyboard

import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseViewModel
import kg.timmitof.keyboard.domain.repository.EmojiRepository
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import org.orbitmvi.orbit.syntax.Syntax

internal class KeyboardViewModel(
    private val keyboardLayoutRepository: KeyboardLayoutRepository,
    private val emojiRepository: EmojiRepository,
) : BaseViewModel<KeyboardState, KeyboardSideEffect, KeyboardEvent>(KeyboardState()) {

    override fun onEvent(event: KeyboardEvent) {
        when (event) {
            is KeyboardEvent.OnKeySelect -> handleKeySelect(event.char)
            is KeyboardEvent.OnEmojiSelect -> handleEmojiSelect(event.emoji)
            is KeyboardEvent.OnShift -> handleShift()
            is KeyboardEvent.OnBackspace -> handleBackspace()
            is KeyboardEvent.OnSpace -> handleSpace()
            is KeyboardEvent.OnEnter -> handleEnter()
            is KeyboardEvent.OnSymbolsSwitch -> switchLayer(KeyboardLayer.SYMBOLS)
            is KeyboardEvent.OnSymbolsAltSwitch -> handleSymbolsAltSwitch()
            is KeyboardEvent.OnAbcSwitch -> switchLayer(KeyboardLayer.LETTERS)
            is KeyboardEvent.OnEmojiSwitch -> handleEmojiSwitch()
            is KeyboardEvent.OnEmojiSearchOpen -> handleEmojiSearchOpen()
            is KeyboardEvent.OnEmojiSearchClose -> switchLayer(KeyboardLayer.EMOJI)
            is KeyboardEvent.OnEmojiSearchQueryChange -> handleSearchQueryChange(event.query)
        }
    }

    override suspend fun Syntax<KeyboardState, BaseSideEffect>.onBootstrap() {
        val keyboardLayout = keyboardLayoutRepository.getLayout(LAYOUT_LETTERS)
        reduce { state.copy(keyboardLayout = keyboardLayout) }

        // Прогреваем кэш остальных слоёв, чтобы переключение было мгновенным
        keyboardLayoutRepository.getLayout(LAYOUT_SYMBOLS)
        keyboardLayoutRepository.getLayout(LAYOUT_SYMBOLS_ALT)
    }

    private fun handleKeySelect(char: String) = intent {
        if (state.layer == KeyboardLayer.EMOJI_SEARCH) {
            updateSearchQuery(state.emojiSearchQuery + char)
        } else {
            postSideEffect(KeyboardSideEffect.CommitText(char))
        }

        if (state.isUpperCase && !state.isCapsLock) {
            reduce { state.copy(isUpperCase = false) }
        }
    }

    private fun handleEmojiSelect(emoji: String) = intent {
        postSideEffect(KeyboardSideEffect.CommitText(emoji))

        val updatedRecent = emojiRepository.addRecentEmoji(emoji)
        reduce { state.copy(recentEmojis = updatedRecent) }
    }

    private fun handleShift() = intent {
        reduce {
            when {
                state.isCapsLock -> state.copy(isUpperCase = false, isCapsLock = false)
                state.isUpperCase -> state.copy(isCapsLock = true)
                else -> state.copy(isUpperCase = true)
            }
        }
    }

    private fun handleBackspace() = intent {
        if (state.layer == KeyboardLayer.EMOJI_SEARCH) {
            if (state.emojiSearchQuery.isNotEmpty()) {
                updateSearchQuery(state.emojiSearchQuery.dropLast(1))
            }
        } else {
            postSideEffect(KeyboardSideEffect.DeleteBackward)
        }
    }

    private fun handleSpace() = intent {
        if (state.layer == KeyboardLayer.EMOJI_SEARCH) {
            updateSearchQuery(state.emojiSearchQuery + " ")
        } else {
            postSideEffect(KeyboardSideEffect.CommitText(" "))
        }
    }

    private fun handleEnter() = intent {
        if (state.layer == KeyboardLayer.EMOJI_SEARCH) {
            applyLayer(KeyboardLayer.EMOJI)
        } else {
            postSideEffect(KeyboardSideEffect.PerformEditorAction)
        }
    }

    private fun handleSymbolsAltSwitch() = intent {
        val next = if (state.layer == KeyboardLayer.SYMBOLS) {
            KeyboardLayer.SYMBOLS_ALT
        } else {
            KeyboardLayer.SYMBOLS
        }
        applyLayer(next)
    }

    private fun switchLayer(layer: KeyboardLayer) = intent {
        applyLayer(layer)
    }

    private fun handleEmojiSwitch() = intent {
        if (state.emojiCategories.isEmpty()) {
            val categories = emojiRepository.getEmojiCategories()
            reduce { state.copy(emojiCategories = categories) }
        }
        val recentEmojis = emojiRepository.getRecentEmojis()
        reduce { state.copy(recentEmojis = recentEmojis) }

        applyLayer(KeyboardLayer.EMOJI)
    }

    private fun handleEmojiSearchOpen() = intent {
        reduce { state.copy(emojiSearchQuery = "", emojiSearchResults = emptyList()) }
        applyLayer(KeyboardLayer.EMOJI_SEARCH)
    }

    private fun handleSearchQueryChange(query: String) = intent {
        updateSearchQuery(query)
    }

    private suspend fun Syntax<KeyboardState, BaseSideEffect>.updateSearchQuery(query: String) {
        val results = emojiRepository.searchEmojis(query)
        reduce { state.copy(emojiSearchQuery = query, emojiSearchResults = results) }
    }

    private suspend fun Syntax<KeyboardState, BaseSideEffect>.applyLayer(layer: KeyboardLayer) {
        val layout = layer.layoutName()
            ?.let { keyboardLayoutRepository.getLayout(it) }
            ?: state.keyboardLayout

        reduce { state.copy(layer = layer, keyboardLayout = layout) }
    }

    private fun KeyboardLayer.layoutName(): String? = when (this) {
        KeyboardLayer.LETTERS -> LAYOUT_LETTERS
        KeyboardLayer.SYMBOLS -> LAYOUT_SYMBOLS
        KeyboardLayer.SYMBOLS_ALT -> LAYOUT_SYMBOLS_ALT
        KeyboardLayer.EMOJI_SEARCH -> LAYOUT_LETTERS
        KeyboardLayer.EMOJI -> null
    }

    companion object {
        private const val LAYOUT_LETTERS = "en_us"
        private const val LAYOUT_SYMBOLS = "symbols"
        private const val LAYOUT_SYMBOLS_ALT = "symbols_alt"
    }
}