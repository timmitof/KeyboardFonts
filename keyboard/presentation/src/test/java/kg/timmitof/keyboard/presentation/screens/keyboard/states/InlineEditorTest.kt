package kg.timmitof.keyboard.presentation.screens.keyboard.states

import org.junit.Assert.assertEquals
import org.junit.Test

class InlineEditorTest {

    private fun InlineEditor.after(vararg effects: KeyboardSideEffect.Input): InlineEditor =
        effects.fold(this) { editor, effect -> editor.apply(effect) }

    @Test
    fun `черновик заменяется целиком и фиксируется`() {
        val editor = InlineEditor(text = "Привет ").after(
            KeyboardSideEffect.Input.SetComposingText("ка"),
            KeyboardSideEffect.Input.SetComposingText("как"),
            KeyboardSideEffect.Input.FinishComposing,
        )

        assertEquals(InlineEditor(text = "Привет как"), editor)
    }

    @Test
    fun `commitText заменяет черновик, как в поле приложения`() {
        val editor = InlineEditor().after(
            KeyboardSideEffect.Input.SetComposingText("првиет"),
            KeyboardSideEffect.Input.CommitText("привет "),
        )

        assertEquals("привет ", editor.text)
    }

    @Test
    fun `автозамена слова перед курсором`() {
        val editor = InlineEditor(text = "Ну, првиет").after(
            KeyboardSideEffect.Input.ReplaceWordBeforeCursor("привет "),
        )

        assertEquals("Ну, привет ", editor.text)
    }

    @Test
    fun `Backspace стирает эмодзи целиком`() {
        val editor = InlineEditor(text = "ок👍").after(KeyboardSideEffect.Input.DeleteBackward)

        assertEquals("ок", editor.text)
    }

    @Test
    fun `Backspace со слайдом выделяет и стирает хвост`() {
        val selected = InlineEditor(text = "раз два").after(KeyboardSideEffect.Input.SelectBeforeCursor(3))
        assertEquals(3, selected.selection)

        assertEquals("раз ", selected.apply(KeyboardSideEffect.Input.DeleteSelection).text)
    }

    @Test
    fun `удаление слова убирает и пробелы после него`() {
        val editor = InlineEditor(text = "раз два  ").after(KeyboardSideEffect.Input.DeleteWordBackward)

        assertEquals("раз ", editor.text)
    }

    @Test
    fun `снимок текста — текст поля`() {
        assertEquals("слово", InlineEditor(text = "одно слово").textContext.composingWord)
    }
}
