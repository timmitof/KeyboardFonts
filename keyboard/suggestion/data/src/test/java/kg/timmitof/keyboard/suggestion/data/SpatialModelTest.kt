package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.keyboard.suggestion.domain.model.SuggestionRequest
import kg.timmitof.keyboard.suggestion.domain.model.TextContext
import kg.timmitof.keyboard.suggestion.domain.model.TypedKey
import kg.timmitof.keyboard.suggestion.domain.model.keyGeometry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpatialModelTest {

    private val qwerty = keyGeometry {
        row { "qwertyuiop".forEach { key(it, 1f) } }
        row { gap(0.5f); "asdfghjkl".forEach { key(it, 1f) }; gap(0.5f) }
        row { gap(1.5f); "zxcvbnm".forEach { key(it, 1f) }; gap(1.5f) }
    }

    @Test
    fun `без касаний соседняя клавиша получает постоянный бонус, дальняя — ничего`() {
        val model = SpatialModel.of(qwerty, query = "thw", touches = emptyList())

        assertEquals(SpatialModel.NEIGHBOR_BONUS, model.bonus("the"))
        assertEquals(0, model.bonus("thp"))
    }

    @Test
    fun `касание у края соседней клавиши дает больше, чем касание в центр`() {
        // Нажата «w»: у правого края — почти «e», в центре — уверенная «w».
        val edge = SpatialModel.of(qwerty, "thw", touches("th", TypedKey('w', offsetX = 0.48f)))
        val center = SpatialModel.of(qwerty, "thw", touches("th", TypedKey('w')))
        val farSide = SpatialModel.of(qwerty, "thw", touches("th", TypedKey('w', offsetX = -0.45f)))

        val edgeBonus = edge.bonus("the")
        val centerBonus = center.bonus("the")
        val farBonus = farSide.bonus("the")

        assertTrue("$edgeBonus > $centerBonus", edgeBonus > centerBonus)
        assertTrue("$centerBonus > $farBonus", centerBonus > farBonus)
        assertTrue(edgeBonus > SpatialModel.NEIGHBOR_BONUS)
        assertTrue(edgeBonus <= SpatialModel.MAX_TOUCH_BONUS)
    }

    @Test
    fun `касания, не совпавшие со словом, не используются`() {
        val model = SpatialModel.of(qwerty, "thw", touches("tx", TypedKey('w', offsetX = 0.48f)))

        assertEquals(SpatialModel.NEIGHBOR_BONUS, model.bonus("the"))
    }

    @Test
    fun `слово на две буквы короче или с тремя заменами бонуса не получает`() {
        val model = SpatialModel.of(qwerty, query = "qwer", touches = emptyList())

        assertEquals(0, model.bonus("qw"))
        assertEquals(0, model.bonus("wert"))
    }

    @Test
    fun `без геометрии бонуса нет`() {
        assertEquals(0, SpatialModel.of(null, "thw", emptyList()).bonus("the"))
    }

    @Test
    fun `касание у границы дает вариант префикса с соседней буквой`() {
        val edge = SpatialModel.of(qwerty, "je", listOf(TypedKey('j', offsetX = -0.48f), TypedKey('e')))

        assertEquals(listOf("he"), edge.prefixVariants())
    }

    @Test
    fun `касание в центр и ввод без касаний вариантов не дают`() {
        val center = SpatialModel.of(qwerty, "je", touches("j", TypedKey('e')))
        val untouched = SpatialModel.of(qwerty, "je", emptyList())

        assertTrue(center.prefixVariants().isEmpty())
        assertTrue(untouched.prefixVariants().isEmpty())
    }

    @Test
    fun `дописывает слово с соседней первой буквой, если палец лег на границу`() {
        // Точных дополнений хватает — поиск опечаток в начале слова не включается, работает только касание.
        val model = modelOf("hello" to 600, "jelly" to 100, "jeans" to 100, "jest" to 100)

        fun suggest(touches: List<TypedKey>) = runBlocking {
            SuggestionEngine().suggest(
                request = SuggestionRequest(
                    languageCode = "en_us",
                    context = TextContext(before = "je"),
                    geometry = qwerty,
                    touches = touches,
                ),
                model = model,
                user = UserLanguageModel(),
            ).map { it.text }
        }

        val onEdge = suggest(listOf(TypedKey('j', offsetX = -0.48f), TypedKey('e')))
        val inCenter = suggest(touches("j", TypedKey('e')))

        assertTrue(onEdge.toString(), "hello" in onEdge)
        assertFalse(inCenter.toString(), "hello" in inCenter)
    }

    @Test
    fun `лишняя буква — двойное касание или клавиша рядом с соседней буквой`() {
        val model = SpatialModel.of(qwerty, query = "thhre", touches = emptyList())

        assertEquals(SpatialModel.REPEAT_BONUS, SpatialModel.of(qwerty, "thhe", emptyList()).bonus("the"))
        // «r» рядом с «e»: задели, нажимая «e».
        assertEquals(SpatialModel.NEIGHBOR_BONUS, SpatialModel.of(qwerty, "thre", emptyList()).bonus("the"))
        // «p» далеко и от «h», и от «e».
        assertEquals(0, SpatialModel.of(qwerty, "thpe", emptyList()).bonus("the"))
        // Две лишние — уже не промах пальцем.
        assertEquals(0, model.bonus("the"))
    }

    @Test
    fun `лишняя буква у самой соседней клавиши дает больше, чем в дальнем краю`() {
        val nearE = SpatialModel.of(qwerty, "thre", touches("th", TypedKey('r', offsetX = -0.48f)) + TypedKey('e'))
        val farFromE = SpatialModel.of(qwerty, "thre", touches("th", TypedKey('r', offsetX = 0.45f)) + TypedKey('e'))

        assertTrue(nearE.bonus("the") > SpatialModel.NEIGHBOR_BONUS)
        assertTrue(farFromE.bonus("the") < SpatialModel.NEIGHBOR_BONUS)
    }

    @Test
    fun `пропущенная буква получает бонус, только если она удвоенная`() {
        assertEquals(SpatialModel.REPEAT_BONUS, SpatialModel.of(qwerty, "leter", emptyList()).bonus("letter"))
        assertEquals(0, SpatialModel.of(qwerty, "lter", emptyList()).bonus("later"))
    }

    @Test
    fun `начало слова сравнивается с учетом лишней и пропущенной буквы`() {
        assertEquals(SpatialModel.REPEAT_BONUS, SpatialModel.of(qwerty, "thhes", emptyList()).prefixBonus("these"))
        assertEquals(SpatialModel.REPEAT_BONUS, SpatialModel.of(qwerty, "lete", emptyList()).prefixBonus("letters"))
    }

    private fun touches(centered: String, last: TypedKey): List<TypedKey> =
        centered.map(::TypedKey) + last
}
