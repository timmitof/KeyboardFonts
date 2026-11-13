package kg.timmitof.keyboard.presentation.screens.keyboard.states

import android.icu.text.BreakIterator
import androidx.compose.runtime.Immutable
import kg.timmitof.keyboard.suggestion.domain.model.TextContext

/**
 * Поле ввода внутри клавиатуры (панель перевода). Понимает те же команды, что и поле приложения,
 * поэтому набор, Т9, автозамена и Backspace работают в нём без отдельной логики.
 *
 * Курсор всегда в конце: область компоновки и выделение — хвост текста.
 *
 * @param composingStart начало области компоновки; -1 — её нет.
 * @param selection сколько символов перед курсором выделено (Backspace со слайдом).
 */
@Immutable
internal data class PanelEditor(
    val text: String = "",
    val composingStart: Int = NO_COMPOSING,
    val selection: Int = 0,
) {

    val textContext: TextContext get() = TextContext(before = text.takeLast(TextContext.MAX_BEFORE_LENGTH))

    fun apply(effect: KeyboardSideEffect.Input): PanelEditor = when (effect) {
        is KeyboardSideEffect.Input.CommitText -> replaceActive(effect.char.toString())
        is KeyboardSideEffect.Input.SetComposingText -> setComposing(effect.text.toString())
        is KeyboardSideEffect.Input.FinishComposing -> copy(composingStart = NO_COMPOSING)
        is KeyboardSideEffect.Input.SelectBeforeCursor -> copy(selection = effect.chars.coerceIn(0, text.length))
        is KeyboardSideEffect.Input.DeleteSelection -> replaceTail(selection, "")
        is KeyboardSideEffect.Input.DeleteBackward ->
            if (selection > 0) replaceTail(selection, "") else replaceTail(lastGraphemeLength(), "")

        is KeyboardSideEffect.Input.DeleteWordBackward -> replaceTail(text.length - text.dropLastWord().length, "")
        is KeyboardSideEffect.Input.ReplaceWordBeforeCursor ->
            replaceTail(text.takeLastWhile { !it.isWordSeparator() }.length, effect.text.toString())

        is KeyboardSideEffect.Input.ReplaceTextBeforeCursor -> replaceTail(effect.chars, effect.text.toString())
        // Курсор в панели всегда в конце, а Enter отправляет перевод — это решает переводчик, не поле.
        is KeyboardSideEffect.Input.MoveCursor,
        is KeyboardSideEffect.Input.PerformEditorAction,
        is KeyboardSideEffect.Input.RefreshTextContext -> this
    }

    /** Как `commitText`: заменяет область компоновки или выделение, иначе дописывает. */
    private fun replaceActive(replacement: String): PanelEditor = when {
        composingStart != NO_COMPOSING -> PanelEditor(text = text.take(composingStart) + replacement)
        else -> replaceTail(selection, replacement)
    }

    private fun setComposing(composing: String): PanelEditor {
        val base = when {
            composingStart != NO_COMPOSING -> text.take(composingStart)
            else -> text.dropLast(selection)
        }
        return PanelEditor(
            text = base + composing,
            composingStart = if (composing.isEmpty()) NO_COMPOSING else base.length,
        )
    }

    private fun replaceTail(chars: Int, replacement: String): PanelEditor =
        PanelEditor(text = text.dropLast(chars.coerceIn(0, text.length)) + replacement)

    /** Эмодзи и буквы со знаками — несколько char, стираем видимый символ целиком. */
    private fun lastGraphemeLength(): Int {
        if (text.isEmpty()) return 0
        val iterator = BreakIterator.getCharacterInstance().apply { setText(text) }
        val start = iterator.preceding(text.length)
        return if (start == BreakIterator.DONE) text.length else text.length - start
    }

    private fun String.dropLastWord(): String = trimEnd().dropLastWhile { !it.isWhitespace() }

    private fun Char.isWordSeparator(): Boolean = isWhitespace() || this in WORD_SEPARATORS

    private companion object {
        const val NO_COMPOSING = -1
        const val WORD_SEPARATORS = ".,!?;:"
    }
}
