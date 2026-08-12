package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.domain.model.KeyCharacter
import kg.timmitof.keyboard.domain.model.WordSuggestion
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax
import kg.timmitof.keyboard.presentation.screens.keyboard.states.AutoCorrection
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardLayer
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect
import kg.timmitof.keyboard.presentation.screens.keyboard.states.ShiftState
import kg.timmitof.keyboard.presentation.screens.keyboard.states.isUpperCase

internal class TextInputDelegate(
    private val layerDelegate: LayerDelegate,
    private val emojiDelegate: EmojiDelegate,
    private val suggestionsDelegate: SuggestionsDelegate,
) {

    /**
     * Ввод символа с клавиши.
     *
     * Регистр выбирается здесь, а не при отрисовке клавиши: при нескольких
     * одновременных нажатиях кнопки успевают захватить состояние Shift,
     * которое к моменту ввода уже устарело — и всё слово уходило заглавными.
     */
    suspend fun KeyboardSyntax.typeCharacter(character: KeyCharacter) {
        val char = character.text(state.shiftState.isUpperCase())

        // Точка, запятая и прочие разделители заканчивают слово так же, как пробел.
        if (char.isSeparator() && state.layer != KeyboardLayer.EMOJI_SEARCH) {
            finishWord(char)
            return
        }

        val styled = state.activeFont.apply(char)
        editText(KeyboardSideEffect.CommitText(styled)) { query -> query + char }
        releaseOneShotShift()

        if (state.layer == KeyboardLayer.EMOJI_SEARCH) return

        forgetAutoCorrection()
        with(suggestionsDelegate) { applyLocalEdit { it.appending(styled) } }
    }

    /**
     * Подставляет подсказку вместо набранного слова.
     */
    suspend fun KeyboardSyntax.applySuggestion(suggestion: WordSuggestion) {
        val styled = state.activeFont.apply(suggestion.text)
        postSideEffect(KeyboardSideEffect.ReplaceWordBeforeCursor("$styled "))

        suggestionsDelegate.learnWord(state, suggestion.text)
        reduce { state.copy(suggestions = emptyList(), autoCorrection = null) }
    }

    suspend fun KeyboardSyntax.typeSpace() = finishWord(" ")

    /**
     * Завершение слова: при необходимости исправляет набранное и запоминает его.
     *
     * Момент, ради которого Т9 и существует — здесь текст в поле становится
     * правильным сам, а клавиатура запоминает пару «предыдущее слово → слово».
     */
    private suspend fun KeyboardSyntax.finishWord(separator: String) {
        if (state.layer == KeyboardLayer.EMOJI_SEARCH) {
            with(emojiDelegate) { updateSearchQuery(state.emojiSearchQuery + separator) }
            return
        }

        val correction = state.pendingAutoCorrect
        val typed = state.textContext.composingWord

        if (correction != null && typed.isNotEmpty()) {
            val corrected = state.activeFont.apply(correction.text) + separator
            postSideEffect(KeyboardSideEffect.ReplaceWordBeforeCursor(corrected))

            suggestionsDelegate.learnWord(state, correction.text)
            reduce {
                state.copy(
                    suggestions = emptyList(),
                    autoCorrection = AutoCorrection(
                        original = typed + separator,
                        corrected = corrected,
                    ),
                )
            }
        } else {
            postSideEffect(KeyboardSideEffect.CommitText(separator))
            if (typed.isNotEmpty()) suggestionsDelegate.learnWord(state, typed)

            forgetAutoCorrection()
            with(suggestionsDelegate) { applyLocalEdit { it.appending(separator) } }
        }
    }

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
            state.textContext.composingWord
                .takeIf { it.isNotEmpty() }
                ?.let { suggestionsDelegate.learnWord(state, it) }

            postSideEffect(KeyboardSideEffect.PerformEditorAction)
            forgetAutoCorrection()
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
    suspend fun KeyboardSyntax.deleteBackward() {
        if (undoAutoCorrection()) return

        editText(KeyboardSideEffect.DeleteBackward) { query ->
            query.ifEmpty { null }?.dropLast(1)
        }
    }

    suspend fun KeyboardSyntax.deleteWordBackward() {
        forgetAutoCorrection()
        editText(KeyboardSideEffect.DeleteWordBackward) { query ->
            query.ifEmpty { null }?.dropLastWord()
        }
    }

    /**
     * Backspace сразу после автозамены возвращает то, что было набрано.
     *
     * Без этого исправление нечем отменить: слово уже заменено, и пользователю
     * пришлось бы стирать его целиком.
     */
    private suspend fun KeyboardSyntax.undoAutoCorrection(): Boolean {
        if (state.layer == KeyboardLayer.EMOJI_SEARCH) return false

        val correction = state.autoCorrection ?: return false
        if (!state.textContext.before.endsWith(correction.corrected)) {
            forgetAutoCorrection()
            return false
        }

        postSideEffect(
            KeyboardSideEffect.ReplaceTextBeforeCursor(
                chars = correction.corrected.length,
                text = correction.original,
            )
        )
        reduce { state.copy(autoCorrection = null) }
        return true
    }

    private suspend fun KeyboardSyntax.forgetAutoCorrection() {
        if (state.autoCorrection != null) {
            reduce { state.copy(autoCorrection = null) }
        }
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

    /** Знаки, после которых слово считается законченным. */
    private fun String.isSeparator(): Boolean = length == 1 && this[0] in WORD_SEPARATORS

    private companion object {
        const val WORD_SEPARATORS = ".,!?;:"
    }
}
