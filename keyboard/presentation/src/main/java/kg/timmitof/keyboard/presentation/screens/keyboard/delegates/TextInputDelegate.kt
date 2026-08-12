package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.domain.model.WordSuggestion
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardLayer
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect
import kg.timmitof.keyboard.presentation.screens.keyboard.states.ShiftState

internal class TextInputDelegate(
    private val layerDelegate: LayerDelegate,
    private val emojiDelegate: EmojiDelegate,
) {

    suspend fun KeyboardSyntax.typeCharacter(char: String) {
        val styled = state.activeFont.apply(char)
        editText(KeyboardSideEffect.CommitText(styled)) { query -> query + char }
        releaseOneShotShift()
    }

    /**
     * Подставляет подсказку вместо набранного слова.
     */
    suspend fun KeyboardSyntax.applySuggestion(suggestion: WordSuggestion) {
        val styled = state.activeFont.apply(suggestion.text)
        postSideEffect(KeyboardSideEffect.ReplaceWordBeforeCursor("$styled "))
        reduce { state.copy(suggestions = emptyList()) }
    }

    suspend fun KeyboardSyntax.typeSpace() =
        editText(KeyboardSideEffect.CommitText(" ")) { query -> "$query " }

    suspend fun KeyboardSyntax.moveCursor(horizontal: Int, vertical: Int) {
        if (state.layer == KeyboardLayer.EMOJI_SEARCH) return
        if (horizontal != 0 || vertical != 0) {
            postSideEffect(KeyboardSideEffect.MoveCursor(horizontal, vertical))
        }
    }

    suspend fun KeyboardSyntax.setCursorMode(active: Boolean) {
        if (state.isCursorMode != active) {
            reduce { state.copy(isCursorMode = active) }
        }
    }

    suspend fun KeyboardSyntax.pressEnter() {
        if (state.layer == KeyboardLayer.EMOJI_SEARCH) {
            with(layerDelegate) { applyLayer(KeyboardLayer.EMOJI) }
        } else {
            postSideEffect(KeyboardSideEffect.PerformEditorAction)
        }
    }

    // region Shift
    suspend fun KeyboardSyntax.toggleShift() {
        reduce {
            when (state.shiftState) {
                ShiftState.DISABLED -> state.copy(shiftState = ShiftState.ACTIVE)
                ShiftState.ACTIVE -> state.copy(shiftState = ShiftState.CAPS_LOCK)
                ShiftState.CAPS_LOCK -> state.copy(shiftState = ShiftState.DISABLED)
            }
        }
    }

    /** Сбрасывает одноразовый shift после ввода символа (caps lock не трогаем). */
    private suspend fun KeyboardSyntax.releaseOneShotShift() {
        if (state.shiftState == ShiftState.ACTIVE) {
            reduce { state.copy(shiftState = ShiftState.DISABLED) }
        }
    }

    /** Полный сброс shift, включая caps lock. */
    suspend fun KeyboardSyntax.resetShift() {
        if (state.shiftState != ShiftState.DISABLED) {
            reduce { state.copy(shiftState = ShiftState.DISABLED) }
        }
    }
    // endregion

    // region Backspace
    suspend fun KeyboardSyntax.deleteBackward() =
        editText(KeyboardSideEffect.DeleteBackward) { query ->
            query.ifEmpty { null }?.dropLast(1)
        }

    suspend fun KeyboardSyntax.deleteWordBackward() =
        editText(KeyboardSideEffect.DeleteWordBackward) { query ->
            query.ifEmpty { null }?.dropLastWord()
        }

    suspend fun KeyboardSyntax.changeBackspaceSelection(chars: Int) {
        if (state.layer == KeyboardLayer.EMOJI_SEARCH) {
            reduce {
                state.copy(emojiSearchSelection = chars.coerceAtMost(state.emojiSearchQuery.length))
            }
        } else {
            postSideEffect(KeyboardSideEffect.SelectBeforeCursor(chars))
        }
    }

    suspend fun KeyboardSyntax.commitBackspaceSelection(chars: Int) {
        if (state.layer == KeyboardLayer.EMOJI_SEARCH) {
            val selected = chars.coerceAtMost(state.emojiSearchQuery.length)
            if (selected > 0) {
                with(emojiDelegate) { updateSearchQuery(state.emojiSearchQuery.dropLast(selected)) }
            } else {
                reduce { state.copy(emojiSearchSelection = 0) }
            }
        } else {
            postSideEffect(KeyboardSideEffect.DeleteSelection)
        }
    }
    // endregion

    /** Маршрутизация правки: поисковый запрос эмодзи или side effect в поле ввода. */
    private suspend fun KeyboardSyntax.editText(
        fieldEffect: KeyboardSideEffect,
        editQuery: (String) -> String?,
    ) {
        if (state.layer == KeyboardLayer.EMOJI_SEARCH) {
            editQuery(state.emojiSearchQuery)?.let { with(emojiDelegate) { updateSearchQuery(it) } }
        } else {
            postSideEffect(fieldEffect)
        }
    }

    private fun String.dropLastWord(): String =
        trimEnd().dropLastWhile { !it.isWhitespace() }
}
