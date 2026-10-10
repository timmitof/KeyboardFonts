package kg.timmitof.keyboard.suggestion.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextContextTest {

    @Test
    fun `набираемое слово — это хвост текста перед курсором`() {
        assertEquals("ми", TextContext(before = "привет ми").composingWord)
        assertEquals("", TextContext(before = "привет ").composingWord)
        assertEquals("", TextContext().composingWord)
    }

    @Test
    fun `внутри слова курсор ничего не набирает`() {
        val context = TextContext(before = "при", after = "вет")

        assertEquals("", context.composingWord)
    }

    @Test
    fun `предыдущее слово — левая часть пары`() {
        assertEquals("привет", TextContext(before = "привет ка").previousWord)
        assertEquals("привет", TextContext(before = "привет ").previousWord)
    }

    @Test
    fun `конец предложения обрывает связь со следующим словом`() {
        assertEquals("", TextContext(before = "привет. ").previousWord)
    }

    @Test
    fun `начало предложения — пустое поле, перевод строки и точка`() {
        assertTrue(TextContext().isSentenceStart)
        assertTrue(TextContext(before = "Как дела? ").isSentenceStart)
        assertTrue(TextContext(before = "Строка\n").isSentenceStart)

        assertFalse(TextContext(before = "привет ").isSentenceStart)
        assertFalse(TextContext(before = "привет прив").isSentenceStart)

        // Первое слово поля тоже начинает предложение.
        assertTrue(TextContext(before = "прив").isSentenceStart)
    }

    @Test
    fun `начало предложения считается от позиции слова, а не курсора`() {
        // Слово уже начали набирать, но стоит оно всё равно в начале предложения.
        assertTrue(TextContext(before = "Привет. С").isSentenceStart)
        assertFalse(TextContext(before = "Привет, с").isSentenceStart)
    }

    @Test
    fun `слова поля не включают набираемое`() {
        val words = TextContext(before = "Тимурыч уже написал: Тим").surroundingWords

        assertTrue("тимурыч" in words.map(String::lowercase))
        assertFalse("Тим" in words)
    }

    @Test
    fun `локальные правки повторяют ввод и удаление`() {
        val typed = TextContext(before = "прив").appending("е")

        assertEquals("приве", typed.before)
        assertEquals("прив", typed.removingLast(1).before)
    }

    @Test
    fun `окно текста не растёт бесконечно`() {
        val long = TextContext(before = "а".repeat(TextContext.MAX_BEFORE_LENGTH)).appending("б")

        assertEquals(TextContext.MAX_BEFORE_LENGTH, long.before.length)
        assertTrue(long.before.endsWith("б"))
    }

    @Test
    fun `стилизованное слово с разрядкой и знаками — одно слово`() {
        assertEquals("П\u202FР\u202FИ\u202F", TextContext(before = "ок П\u202FР\u202FИ\u202F").composingWord)
        assertEquals("п\u0336р\u0336", TextContext(before = "ок п\u0336р\u0336").composingWord)
        assertEquals("ок", TextContext(before = "ок д\u0336").previousWord)
    }
}
