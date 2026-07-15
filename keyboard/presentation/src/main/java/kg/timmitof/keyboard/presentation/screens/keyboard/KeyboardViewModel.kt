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
            is KeyboardEvent.OnSpace -> handleSpace()
            is KeyboardEvent.OnEnter -> handleEnter()
            is KeyboardEvent.OnShift -> handleShift()
            is KeyboardEvent.OnBackspace -> handleBackspace()
            is KeyboardEvent.OnBackspaceDeleteWord -> handleBackspaceDeleteWord()
            is KeyboardEvent.OnBackspaceSelectChange -> handleBackspaceSelectChange(event.chars)
            is KeyboardEvent.OnBackspaceSelectCommit -> handleBackspaceSelectCommit(event.chars)
            is KeyboardEvent.OnSymbolsSwitch -> switchLayer(KeyboardLayer.SYMBOLS)
            is KeyboardEvent.OnSymbolsAltSwitch -> handleSymbolsAltSwitch()
            is KeyboardEvent.OnAbcSwitch -> switchLayer(KeyboardLayer.LETTERS)
            is KeyboardEvent.OnEmojiSwitch -> handleEmojiSwitch()
            is KeyboardEvent.OnEmojiSelect -> handleEmojiSelect(event.emoji)
            is KeyboardEvent.OnEmojiSearchOpen -> handleEmojiSearchOpen()
            is KeyboardEvent.OnEmojiSearchClose -> switchLayer(KeyboardLayer.EMOJI)
            is KeyboardEvent.OnEmojiSearchQueryChange -> handleSearchQueryChange(event.query)
        }
    }

    override suspend fun Syntax<KeyboardState, BaseSideEffect>.onBootstrap() {
        applyLayer(KeyboardLayer.LETTERS)

        // Прогреваем кэш остальных раскладок, чтобы переключение слоёв было мгновенным
        KeyboardLayer.entries
            .mapNotNull { it.layoutName }
            .distinct()
            .forEach { keyboardLayoutRepository.getLayout(it) }
    }

    // region Input Text
    private fun handleKeySelect(char: String) {
        editText(KeyboardSideEffect.CommitText(char)) { query -> query + char }
        releaseOneShotShift()
    }

    private fun handleSpace() =
        editText(KeyboardSideEffect.CommitText(" ")) { query -> "$query " }

    private fun handleEnter() = intent {
        if (state.layer == KeyboardLayer.EMOJI_SEARCH) {
            applyLayer(KeyboardLayer.EMOJI)
        } else {
            postSideEffect(KeyboardSideEffect.PerformEditorAction)
        }
    }
    // endregion

    // region Shift
    private fun handleShift() = intent {
        reduce {
            when {
                state.isCapsLock -> state.copy(isUpperCase = false, isCapsLock = false)
                state.isUpperCase -> state.copy(isCapsLock = true)
                else -> state.copy(isUpperCase = true)
            }
        }
    }

    /** Сбрасывает одноразовый shift после ввода символа (caps lock не трогаем). */
    private fun releaseOneShotShift() = intent {
        if (state.isUpperCase && !state.isCapsLock) {
            reduce { state.copy(isUpperCase = false) }
        }
    }
    // endregion

    // region Backspace
    private fun handleBackspace() =
        editText(KeyboardSideEffect.DeleteBackward) { query ->
            query.ifEmpty { null }?.dropLast(1)
        }

    private fun handleBackspaceDeleteWord() =
        editText(KeyboardSideEffect.DeleteWordBackward) { query ->
            query.ifEmpty { null }?.dropLastWord()
        }

    private fun handleBackspaceSelectChange(chars: Int) = intent {
        if (state.layer == KeyboardLayer.EMOJI_SEARCH) {
            reduce {
                state.copy(emojiSearchSelection = chars.coerceAtMost(state.emojiSearchQuery.length))
            }
        } else {
            postSideEffect(KeyboardSideEffect.SelectBeforeCursor(chars))
        }
    }

    private fun handleBackspaceSelectCommit(chars: Int) = intent {
        if (state.layer == KeyboardLayer.EMOJI_SEARCH) {
            val selected = chars.coerceAtMost(state.emojiSearchQuery.length)
            if (selected > 0) {
                updateSearchQuery(state.emojiSearchQuery.dropLast(selected))
            } else {
                reduce { state.copy(emojiSearchSelection = 0) }
            }
        } else {
            postSideEffect(KeyboardSideEffect.DeleteSelection)
        }
    }
    // endregion

    // region Layers
    private fun switchLayer(layer: KeyboardLayer) = intent {
        applyLayer(layer)
    }

    private fun handleSymbolsAltSwitch() = intent {
        val next = if (state.layer == KeyboardLayer.SYMBOLS) {
            KeyboardLayer.SYMBOLS_ALT
        } else {
            KeyboardLayer.SYMBOLS
        }
        applyLayer(next)
    }

    /** Переключает слой, подгружая его раскладку */
    private suspend fun Syntax<KeyboardState, BaseSideEffect>.applyLayer(layer: KeyboardLayer) {
        val layout = layer.layoutName
            ?.let { keyboardLayoutRepository.getLayout(it) }
            ?: state.keyboardLayout

        reduce { state.copy(layer = layer, keyboardLayout = layout) }
    }
    // endregion

    // region Emoji
    private fun handleEmojiSelect(emoji: String) = intent {
        postSideEffect(KeyboardSideEffect.CommitText(emoji))

        val updatedRecent = emojiRepository.addRecentEmoji(emoji)
        reduce { state.copy(recentEmojis = updatedRecent) }
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
        reduce {
            state.copy(
                emojiSearchQuery = query,
                emojiSearchResults = results,
                emojiSearchSelection = 0
            )
        }
    }
    // endregion

    // region common
    private fun editText(
        fieldEffect: KeyboardSideEffect,
        editQuery: (String) -> String?,
    ) = intent {
        if (state.layer == KeyboardLayer.EMOJI_SEARCH) {
            editQuery(state.emojiSearchQuery)?.let { updateSearchQuery(it) }
        } else {
            postSideEffect(fieldEffect)
        }
    }

    private fun String.dropLastWord(): String =
        trimEnd().dropLastWhile { !it.isWhitespace() }
    // endregion
}
