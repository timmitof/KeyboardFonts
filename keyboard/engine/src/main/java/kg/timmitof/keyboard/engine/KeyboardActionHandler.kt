package kg.timmitof.keyboard.engine

import android.icu.text.BreakIterator
import android.os.SystemClock
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect
import kotlin.math.abs

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
            is KeyboardSideEffect.ReplaceWordBeforeCursor -> connection.replaceWordBeforeCursor(action.text)
            is KeyboardSideEffect.ReplaceTextBeforeCursor -> connection.replaceBeforeCursor(action.chars, action.text)
            is KeyboardSideEffect.MoveCursor -> connection.moveCursor(action.horizontal, action.vertical)
            is KeyboardSideEffect.DeleteBackward -> connection.deleteLastGrapheme()
            is KeyboardSideEffect.DeleteWordBackward -> connection.deleteWordBeforeCursor()
            is KeyboardSideEffect.SelectBeforeCursor -> connection.selectBeforeCursor(action.chars)
            is KeyboardSideEffect.PerformEditorAction -> connection.performEnter()
            is KeyboardSideEffect.DeleteSelection -> connection.commitText("", 1)
        }
    }

    /**
     * Заменяет незаконченное слово перед курсором на [text].
     */
    private fun InputConnection.replaceWordBeforeCursor(text: CharSequence) {
        val before = getTextBeforeCursor(WORD_LOOKUP_LENGTH, 0)?.toString().orEmpty()
        val typed = before.takeLastWhile { !it.isWordSeparator() }
        replaceBeforeCursor(typed.length, text)
    }

    /** Меняет [chars] символов перед курсором на [text] одной правкой. */
    private fun InputConnection.replaceBeforeCursor(chars: Int, text: CharSequence) {
        beginBatchEdit()
        if (chars > 0) deleteSurroundingText(chars, 0)
        commitText(text, 1)
        endBatchEdit()
    }

    /** Граница слова: пробелы и знаки препинания в подсказку не входят. */
    private fun Char.isWordSeparator(): Boolean = isWhitespace() || this in WORD_SEPARATORS

    private fun InputConnection.performEnter() {
        val editorInfo = editorInfoProvider()
        if (editorInfo.hasEditorAction()) {
            performEditorAction(editorInfo.editorActionId())
        } else {
            sendEnterKey()
        }
    }

    private fun InputConnection.sendEnterKey() {
        val now = SystemClock.uptimeMillis()
        sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0))
        sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER, 0))
    }

    /**
     * Двигает курсор: [horizontal] символов (+вправо/−влево) и [vertical] строк (+вниз/−вверх)
     */
    private fun InputConnection.moveCursor(horizontal: Int, vertical: Int) {
        val horizontalKey = if (horizontal >= 0) KeyEvent.KEYCODE_DPAD_RIGHT else KeyEvent.KEYCODE_DPAD_LEFT
        repeat(abs(horizontal)) { sendKey(horizontalKey) }

        val verticalKey = if (vertical >= 0) KeyEvent.KEYCODE_DPAD_DOWN else KeyEvent.KEYCODE_DPAD_UP
        repeat(abs(vertical)) { sendKey(verticalKey) }
    }

    private fun InputConnection.sendKey(keyCode: Int) {
        val now = SystemClock.uptimeMillis()
        sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0))
        sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0))
    }

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

        /** Знаки, которые не входят в слово (совпадают с разбором в `TextContext`). */
        const val WORD_SEPARATORS = ".,!?;:()[]{}<>\"«»„“”…—–-/\\|@#\$%^&*+=~`№"

        /** Сколько символов хватает для поиска границы графемы. */
        const val GRAPHEME_LOOKUP_LENGTH = 32
    }
}
