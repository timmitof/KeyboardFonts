package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.domain.model.KeyCharacter
import kg.timmitof.keyboard.suggestion.domain.model.TextContext
import kg.timmitof.keyboard.suggestion.domain.model.WordSuggestion
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax
import kg.timmitof.keyboard.presentation.screens.keyboard.states.AutoCorrection
import kg.timmitof.keyboard.presentation.screens.keyboard.states.ComposingText
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
        editText(typeEffect(styled)) { query -> query + char }
        releaseOneShotShift()

        if (state.layer == KeyboardLayer.EMOJI_SEARCH) return

        forgetAutoCorrection()
        with(suggestionsDelegate) { applyLocalEdit { it.appending(styled) } }
    }

    /**
     * Буква либо продолжает черновик, либо вписывается начисто.
     *
     * Черновик отправляется целиком: `setComposingText` заменяет всю область
     * компоновки, поэтому досылать один символ нельзя.
     */
    private suspend fun KeyboardSyntax.typeEffect(styled: String): KeyboardSideEffect.Input {
        if (!state.canStartComposing || !styled.isWordText()) {
            closeComposing()
            return KeyboardSideEffect.Input.CommitText(styled)
        }

        val composing = state.composing.let { it.copy(text = it.text + styled) }
        reduce { state.copy(composing = composing) }

        return KeyboardSideEffect.Input.SetComposingText(composing.text, composing.hasCorrection)
    }

    /**
     * Подставляет подсказку вместо набранного слова.
     */
    suspend fun KeyboardSyntax.applySuggestion(suggestion: WordSuggestion) {
        val styled = state.activeFont.apply(suggestion.text)
        replaceWord("$styled ")

        suggestionsDelegate.learnWord(state, suggestion.text)
        reduce { state.copy(suggestions = emptyList(), suggestionsWord = "", autoCorrection = null) }
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

        val typed = state.textContext.composingWord
        // Подстановку пробелом можно выключить: тогда подсказки остаются, но принимает их только тап.
        val correction = typed.takeIf { it.isNotEmpty() && state.settings.isSpaceCommitsEnabled }
            ?.let { suggestionsDelegate.awaitCorrection(state) }

        if (correction != null) {
            val corrected = state.activeFont.apply(correction.text) + separator
            replaceWord(corrected)

            suggestionsDelegate.learnWord(state, correction.text)
            reduce {
                state.copy(
                    suggestions = emptyList(),
                    suggestionsWord = "",
                    autoCorrection = AutoCorrection(
                        original = typed + separator,
                        corrected = corrected,
                    ),
                )
            }
            with(suggestionsDelegate) {
                applyLocalEdit { it.removingLast(typed.length).appending(corrected) }
            }
        } else {
            closeComposing()
            postSideEffect(KeyboardSideEffect.Input.CommitText(separator))
            if (typed.isNotEmpty()) suggestionsDelegate.learnWord(state, typed)

            forgetAutoCorrection()
            with(suggestionsDelegate) { applyLocalEdit { it.appending(separator) } }
        }
    }

    /**
     * Меняет набранное слово на [replacement].
     *
     * Черновик заменяется целиком одним вызовом. Без него остаётся прежний путь
     * с чтением поля: черновика нет как раз тогда, когда курсор поставили посреди
     * чужого текста, и границы слова лучше спросить у самого поля.
     */
    private suspend fun KeyboardSyntax.replaceWord(replacement: String) {
        if (state.composing.isActive) {
            postSideEffect(KeyboardSideEffect.Input.SetComposingText(replacement))
            closeComposing()
        } else {
            postSideEffect(KeyboardSideEffect.Input.ReplaceWordBeforeCursor(replacement))
        }
    }

    /**
     * Сверяет черновик с тем, что реально в поле.
     *
     * Текст меняет не только набор: пользователь ставит курсор в другое место,
     * приложение подставляет своё. Как только слово перестало совпадать
     * с черновиком, область компоновки уже не наша — забываем про неё.
     */
    suspend fun KeyboardSyntax.reconcileComposing(context: TextContext) {
        if (!state.composing.isActive || context.composingWord == state.composing.text) return

        closeComposing()
    }

    /** Закрывает черновик, если он открыт: дальше текст правится не им. */
    suspend fun KeyboardSyntax.closeComposing() {
        if (!state.composing.isActive) return

        postSideEffect(KeyboardSideEffect.Input.FinishComposing)
        reduce { state.copy(composing = ComposingText()) }
    }

    suspend fun KeyboardSyntax.moveCursor(horizontal: Int, vertical: Int) {
        if (state.layer == KeyboardLayer.EMOJI_SEARCH) return
        if (horizontal != 0 || vertical != 0) {
            closeComposing()
            postSideEffect(KeyboardSideEffect.Input.MoveCursor(horizontal, vertical))
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

            closeComposing()
            postSideEffect(KeyboardSideEffect.Input.PerformEditorAction)
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
        if (shrinkComposing()) return

        editText(KeyboardSideEffect.Input.DeleteBackward) { query ->
            query.ifEmpty { null }?.dropLast(1)
        }
    }

    /**
     * Backspace внутри черновика сокращает его целиком, а не удаляет символ в поле:
     * область компоновки правится только заменой, иначе она разъедется с текстом.
     */
    private suspend fun KeyboardSyntax.shrinkComposing(): Boolean {
        val composing = state.composing
        if (!composing.isActive) return false

        val shortened = composing.copy(text = composing.text.dropLastCodePoint())
        postSideEffect(
            KeyboardSideEffect.Input.SetComposingText(shortened.text, shortened.hasCorrection)
        )
        if (!shortened.isActive) postSideEffect(KeyboardSideEffect.Input.FinishComposing)

        reduce { state.copy(composing = shortened) }
        with(suggestionsDelegate) {
            applyLocalEdit { it.removingLast(composing.text.length - shortened.text.length) }
        }
        return true
    }

    suspend fun KeyboardSyntax.deleteWordBackward() {
        forgetAutoCorrection()
        closeComposing()
        editText(KeyboardSideEffect.Input.DeleteWordBackward) { query ->
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
            KeyboardSideEffect.Input.ReplaceTextBeforeCursor(
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
            closeComposing()
            postSideEffect(KeyboardSideEffect.Input.SelectBeforeCursor(chars))
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
            closeComposing()
            postSideEffect(KeyboardSideEffect.Input.DeleteSelection)
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

    /** Убирает последний символ целиком: стилизованные буквы — суррогатные пары. */
    private fun String.dropLastCodePoint(): String =
        if (isEmpty()) this else dropLast(Character.charCount(codePointBefore(length)))

    /** Продолжает ли текст слово — правило то же, что у снимка вокруг курсора. */
    private fun String.isWordText(): Boolean = all(TextContext.Companion::isWordChar)

    /** Знаки, после которых слово считается законченным. */
    private fun String.isSeparator(): Boolean = length == 1 && this[0] in WORD_SEPARATORS

    private companion object {
        const val WORD_SEPARATORS = ".,!?;:"
    }
}
