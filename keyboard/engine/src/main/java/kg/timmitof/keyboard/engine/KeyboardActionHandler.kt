package kg.timmitof.keyboard.engine

import android.icu.text.BreakIterator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSideEffect

/**
 * Применяет side effects клавиатуры к полю ввода
 *
 * [InputConnection] и [EditorInfo] запрашиваются через провайдеры на каждое действие,
 * потому что у IME они меняются при каждой смене поля ввода
 *
 * @param inputConnectionProvider доступ к актуальному [InputConnection]
 * @param editorInfoProvider доступ к актуальному [EditorInfo]
 */
internal class KeyboardActionHandler(
    private val inputConnectionProvider: () -> InputConnection?,
    private val editorInfoProvider: () -> EditorInfo?,
) {

    fun handle(action: KeyboardSideEffect) {
        val connection = inputConnectionProvider() ?: return
        when (action) {
            is KeyboardSideEffect.CommitText -> connection.commitText(action.char, 1)
            is KeyboardSideEffect.DeleteBackward -> connection.deleteLastGrapheme()
            is KeyboardSideEffect.DeleteWordBackward -> connection.deleteWordBeforeCursor()
            is KeyboardSideEffect.SelectBeforeCursor -> connection.selectBeforeCursor(action.chars)
            is KeyboardSideEffect.PerformEditorAction -> connection.performEditorAction(editorAction())
            is KeyboardSideEffect.DeleteSelection -> connection.commitText("", 1)
        }
    }

    /** Действие редактора из imeOptions текущего поля (Done/Next/Search и т. д.). */
    private fun editorAction(): Int = editorInfoProvider()?.imeOptions
        ?.and(EditorInfo.IME_MASK_ACTION)
        ?: EditorInfo.IME_ACTION_UNSPECIFIED

    /**
     * Удаляет последний графемный кластер перед курсором.
     */
    private fun InputConnection.deleteLastGrapheme() {
        val before = getTextBeforeCursor(GRAPHEME_LOOKUP_LENGTH, 0)
        if (before.isNullOrEmpty()) return

        val iterator = BreakIterator.getCharacterInstance()
        iterator.setText(before.toString())
        val end = iterator.last()
        val start = iterator.previous().takeIf { it != BreakIterator.DONE } ?: 0

        deleteSurroundingText(end - start, 0)
    }

    /** Удаляет слово перед курсором: хвостовые пробелы + текст до предыдущего пробела. */
    private fun InputConnection.deleteWordBeforeCursor() {
        val before = getTextBeforeCursor(WORD_LOOKUP_LENGTH, 0)
        if (before.isNullOrEmpty()) return

        var index = before.length - 1
        while (index >= 0 && before[index].isWhitespace()) index--
        while (index >= 0 && !before[index].isWhitespace()) index--

        val deleteCount = before.length - 1 - index
        if (deleteCount > 0) {
            deleteSurroundingText(deleteCount, 0)
        }
    }

    /** Выделяет [chars] символов назад от курсора, не двигая его конец. */
    private fun InputConnection.selectBeforeCursor(chars: Int) {
        val extracted = getExtractedText(ExtractedTextRequest(), 0) ?: return
        val text = extracted.text?.toString() ?: return

        // Якорь выделения - позиция курсора
        val anchor = extracted.selectionEnd
        var start = (anchor - chars).coerceAtLeast(0)

        // Начало не должно попадать внутрь эмодзи - сдвигаем к границе графемы
        val iterator = BreakIterator.getCharacterInstance()
        iterator.setText(text)
        if (start in 1 until text.length && !iterator.isBoundary(start)) {
            start = iterator.preceding(start).takeIf { it != BreakIterator.DONE } ?: 0
        }

        setSelection(extracted.startOffset + start, extracted.startOffset + anchor)
    }

    private companion object {
        /** Сколько символов перед курсором запрашивать для поиска границы слова. */
        const val WORD_LOOKUP_LENGTH = 64

        /** Сколько символов хватает для поиска границы графемы. */
        const val GRAPHEME_LOOKUP_LENGTH = 32
    }
}
