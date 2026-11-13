package kg.timmitof.keyboard.presentation.screens.keyboard.states

import androidx.compose.runtime.Immutable
import kg.timmitof.keyboard.suggestion.domain.model.TextContext
import java.text.BreakIterator

/**
 * Поле ввода внутри клавиатуры: поиск эмодзи, панель перевода. Понимает те же команды, что и поле
 * приложения, поэтому набор, Т9, автозамена и Backspace работают в нём без отдельной логики (см. `input`).
 *
 * Курсор всегда в конце: область компоновки и выделение — хвост текста.
 *
 * @param composingStart начало области компоновки; [NO_COMPOSING] — её нет.
 * @param selection сколько символов перед курсором выделено (Backspace со слайдом).
 */
@Immutable
internal data class InlineEditor(
    val text: String = "",
    val composingStart: Int = NO_COMPOSING,
    val selection: Int = 0,
) {

    val textContext: TextContext get() = TextContext(before = text.takeLast(TextContext.MAX_BEFORE_LENGTH))

    fun apply(effect: KeyboardSideEffect.Input): InlineEditor = when (effect) {
        is KeyboardSideEffect.Input.CommitText -> replaceActive(effect.char.toString())
        is KeyboardSideEffect.Input.SetComposingText -> setComposing(effect.text.toString())
        is KeyboardSideEffect.Input.FinishComposing -> copy(composingStart = NO_COMPOSING)
        is KeyboardSideEffect.Input.SelectBeforeCursor -> copy(selection = effect.chars.coerceIn(0, text.length))
        is KeyboardSideEffect.Input.DeleteSelection -> replaceTail(selection, "")
        is KeyboardSideEffect.Input.DeleteBackward ->
            replaceTail(if (selection > 0) selection else lastGraphemeLength(), "")

        is KeyboardSideEffect.Input.DeleteWordBackward -> replaceTail(text.length - text.dropLastWord().length, "")
        is KeyboardSideEffect.Input.ReplaceWordBeforeCursor ->
            replaceTail(text.takeLastWhile { !it.isWordSeparator() }.length, effect.text.toString())

        is KeyboardSideEffect.Input.ReplaceTextBeforeCursor -> replaceTail(effect.chars, effect.text.toString())
        // Курсор всегда в конце, а Enter решает владелец поля (поиск, переводчик), не само поле.
        is KeyboardSideEffect.Input.MoveCursor,
        is KeyboardSideEffect.Input.PerformEditorAction,
        is KeyboardSideEffect.Input.RefreshTextContext -> this
    }

    /** Как `commitText`: заменяет область компоновки или выделение, иначе дописывает. */
    private fun replaceActive(replacement: String): InlineEditor =
        if (composingStart != NO_COMPOSING) {
            InlineEditor(text = text.take(composingStart) + replacement)
        } else {
            replaceTail(selection, replacement)
        }

    private fun setComposing(composing: String): InlineEditor {
        val base = if (composingStart != NO_COMPOSING) text.take(composingStart) else text.dropLast(selection)
        return InlineEditor(
            text = base + composing,
            composingStart = if (composing.isEmpty()) NO_COMPOSING else base.length,
        )
    }

    private fun replaceTail(chars: Int, replacement: String): InlineEditor =
        InlineEditor(text = text.dropLast(chars.coerceIn(0, text.length)) + replacement)

    /** Эмодзи и буквы со знаками — несколько char, стираем видимый символ целиком. */
    private fun lastGraphemeLength(): Int {
        if (text.isEmpty()) return 0
        // Идём вперёд по хвосту: обратный обход (preceding/previous) на JDK 21 ломается на эмодзи,
        // а прямой одинаково работает и там, и на Android. Хвоста хватает на самый длинный эмодзи.
        val tail = text.takeLast(GRAPHEME_LOOKUP_LENGTH)
        val iterator = BreakIterator.getCharacterInstance().apply { setText(tail) }
        var lastStart = iterator.first()
        var boundary = lastStart
        while (true) {
            val next = iterator.next()
            if (next == BreakIterator.DONE) break
            lastStart = boundary
            boundary = next
        }
        return tail.length - lastStart
    }

    private fun String.dropLastWord(): String = trimEnd().dropLastWhile { !it.isWhitespace() }

    private fun Char.isWordSeparator(): Boolean = isWhitespace() || this in WORD_SEPARATORS

    private companion object {
        const val NO_COMPOSING = -1
        const val WORD_SEPARATORS = ".,!?;:"
        const val GRAPHEME_LOOKUP_LENGTH = 32
    }
}

/** Поле внутри клавиатуры, куда сейчас идёт ввод клавиш. */
internal enum class InlineTarget { EMOJI_SEARCH, TRANSLATOR }
