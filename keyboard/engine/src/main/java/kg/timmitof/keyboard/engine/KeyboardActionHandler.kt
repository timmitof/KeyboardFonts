package kg.timmitof.keyboard.engine

import android.content.Context
import android.icu.text.BreakIterator
import android.os.SystemClock
import android.text.SpannableString
import android.text.Spanned
import android.text.style.SuggestionSpan
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect
import kotlin.math.abs

/**
 * Применяет side effects к полю ввода. [InputConnection] и [EditorInfo] берутся через провайдеры,
 * потому что меняются при каждой смене поля. [context] нужен спану подсказки (локаль).
 */
internal class KeyboardActionHandler(
    private val context: Context,
    private val inputConnectionProvider: () -> InputConnection?,
    private val editorInfoProvider: () -> EditorInfo?,
) {

    fun handle(action: KeyboardSideEffect.Input) {
        val connection = inputConnectionProvider() ?: return
        when (action) {
            is KeyboardSideEffect.Input.CommitText -> connection.commitText(action.char, 1)
            is KeyboardSideEffect.Input.SetComposingText ->
                connection.setComposingText(action.text.withCorrectionHint(action.hasCorrection), 1)

            is KeyboardSideEffect.Input.FinishComposing -> connection.finishComposingText()
            is KeyboardSideEffect.Input.ReplaceWordBeforeCursor -> connection.replaceWordBeforeCursor(action.text)
            is KeyboardSideEffect.Input.ReplaceTextBeforeCursor -> connection.replaceBeforeCursor(action.chars, action.text)
            is KeyboardSideEffect.Input.MoveCursor -> connection.moveCursor(action.horizontal, action.vertical)
            is KeyboardSideEffect.Input.DeleteBackward -> connection.deleteLastGrapheme()
            is KeyboardSideEffect.Input.DeleteWordBackward -> connection.deleteWordBeforeCursor()
            is KeyboardSideEffect.Input.SelectBeforeCursor -> connection.selectBeforeCursor(action.chars)
            is KeyboardSideEffect.Input.PerformEditorAction -> connection.performEnter()
            is KeyboardSideEffect.Input.DeleteSelection -> connection.commitText("", 1)
        }
    }

    private fun CharSequence.withCorrectionHint(hasCorrection: Boolean): CharSequence {
        if (!hasCorrection) return this

        return SpannableString(this).apply {
            setSpan(
                SuggestionSpan(context, arrayOf(), SuggestionSpan.FLAG_AUTO_CORRECTION),
                0,
                length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
    }

    private fun InputConnection.replaceWordBeforeCursor(text: CharSequence) {
        val before = getTextBeforeCursor(WORD_LOOKUP_LENGTH, 0)?.toString().orEmpty()
        val typed = before.takeLastWhile { !it.isWordSeparator() }
        replaceBeforeCursor(typed.length, text)
    }

    private fun InputConnection.replaceBeforeCursor(chars: Int, text: CharSequence) {
        beginBatchEdit()
        if (chars > 0) deleteSurroundingText(chars, 0)
        commitText(text, 1)
        endBatchEdit()
    }

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

    private fun InputConnection.deleteLastGrapheme() {
        val before = getTextBeforeCursor(GRAPHEME_LOOKUP_LENGTH, 0)
        if (before.isNullOrEmpty()) return

        val iterator = BreakIterator.getCharacterInstance()
        iterator.setText(before.toString())
        val end = iterator.last()
        val start = iterator.previous().takeIf { it != BreakIterator.DONE } ?: 0

        deleteSurroundingText(end - start, 0)
    }

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

    private fun InputConnection.selectBeforeCursor(chars: Int) {
        val extracted = getExtractedText(ExtractedTextRequest(), 0) ?: return
        val text = extracted.text?.toString() ?: return

        val anchor = extracted.selectionEnd
        var start = (anchor - chars).coerceAtLeast(0)

        // Начало не должно попадать внутрь эмодзи — сдвигаем к границе графемы.
        val iterator = BreakIterator.getCharacterInstance()
        iterator.setText(text)
        if (start in 1 until text.length && !iterator.isBoundary(start)) {
            start = iterator.preceding(start).takeIf { it != BreakIterator.DONE } ?: 0
        }

        setSelection(extracted.startOffset + start, extracted.startOffset + anchor)
    }

    private companion object {
        const val WORD_LOOKUP_LENGTH = 64

        // Совпадает с разбором в TextContext.
        const val WORD_SEPARATORS = ".,!?;:()[]{}<>\"«»„“”…—–-/\\|@#\$%^&*+=~`№"

        const val GRAPHEME_LOOKUP_LENGTH = 32
    }
}
