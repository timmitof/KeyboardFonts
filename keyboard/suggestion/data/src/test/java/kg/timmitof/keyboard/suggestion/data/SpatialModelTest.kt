package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.keyboard.suggestion.domain.model.TypedKey
import kg.timmitof.keyboard.suggestion.domain.model.keyGeometry
import org.junit.Assert.assertEquals
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
    fun `слово другой длины или с тремя заменами бонуса не получает`() {
        val model = SpatialModel.of(qwerty, query = "qwer", touches = emptyList())

        assertEquals(0, model.bonus("qwe"))
        assertEquals(0, model.bonus("wert"))
    }

    @Test
    fun `без геометрии бонуса нет`() {
        assertEquals(0, SpatialModel.of(null, "thw", emptyList()).bonus("the"))
    }

    private fun touches(centered: String, last: TypedKey): List<TypedKey> =
        centered.map(::TypedKey) + last
}
