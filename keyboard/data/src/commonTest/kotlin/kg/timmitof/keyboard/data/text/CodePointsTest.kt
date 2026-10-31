package kg.timmitof.keyboard.data.text

import kotlin.test.Test
import kotlin.test.assertEquals

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
}
