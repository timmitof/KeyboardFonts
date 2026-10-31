package kg.timmitof.keyboard.font.domain.text

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CodePointsTest {

    @Test
    fun `символ из базовой плоскости — одна кодовая точка`() {
        assertEquals('ж'.code, "жук".codePointAtIndex(0))
        assertEquals(1, charCount('ж'.code))
        assertEquals("ж", codePointToString('ж'.code))
    }

    @Test
    fun `суррогатная пара собирается в одну кодовую точку`() {
        val bold = "𝐀"

        assertEquals(0x1D400, bold.codePointAtIndex(0))
        assertEquals(2, charCount(0x1D400))
        assertEquals(bold, codePointToString(0x1D400))
    }

    @Test
    fun `одиночный суррогат возвращается как есть`() {
        assertEquals(0xD835, "\uD835a".codePointAtIndex(0))
    }

    @Test
    fun `эмодзи с тоном кожи раскладывается на кодовые точки`() {
        assertEquals(listOf(0x1F44B, 0x1F3FD), "👋🏽".codePointList())
    }

    @Test
    fun `кодовая точка перед позицией и их число`() {
        val text = "a\uD835\uDC00"

        assertEquals(0x1D400, text.codePointBeforeIndex(text.length))
        assertEquals('a'.code, text.codePointBeforeIndex(1))
        assertEquals(2, text.codePointCount())
    }

    @Test
    fun `стилизованные буквы и цифры классифицируются как обычные`() {
        assertTrue(isLetterOrDigit('ж'.code))
        assertTrue(isLetterOrDigit(0x1D400))
        assertTrue(isLetterOrDigit(0x1D7CF))
        assertFalse(isLetterOrDigit('.'.code))
        assertFalse(isLetterOrDigit(0x1F130))

        assertTrue(isUpperCase('Ж'.code))
        assertTrue(isUpperCase(0x1D400))
        assertFalse(isUpperCase('ж'.code))
        assertFalse(isUpperCase(0x1D41A))
    }
}
