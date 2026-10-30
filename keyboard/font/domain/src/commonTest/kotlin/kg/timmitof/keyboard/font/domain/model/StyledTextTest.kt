package kg.timmitof.keyboard.font.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StyledTextTest {

    @Test
    fun `хвост буквы — комбинируемые знаки и разрядка`() {
        assertTrue(StyledText.isLetterTail('̶'))
        assertTrue(StyledText.isLetterTail('⃝'))
        assertTrue(StyledText.isLetterTail(StyledText.LETTER_SPACING))
        assertFalse(StyledText.isLetterTail('з'))
        assertFalse(StyledText.isLetterTail(' '))
    }

    @Test
    fun `последний символ стирается вместе с хвостом`() {
        assertEquals(1, StyledText.lastSymbolLength("аб"))
        assertEquals(2, StyledText.lastSymbolLength("аз̶"))
        assertEquals(2, StyledText.lastSymbolLength("аЗ${StyledText.LETTER_SPACING}"))
    }

    @Test
    fun `суррогатная пара — один символ`() {
        assertEquals(2, StyledText.lastSymbolLength("а𝐀"))
        assertEquals(3, StyledText.lastSymbolLength("а𝐀̶"))
    }

    @Test
    fun `одни хвосты стираются целиком`() {
        assertEquals(2, StyledText.lastSymbolLength("̶̶"))
    }
}
