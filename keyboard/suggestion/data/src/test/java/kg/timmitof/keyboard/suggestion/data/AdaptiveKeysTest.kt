package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.keyboard.suggestion.domain.model.TypedKey
import kg.timmitof.keyboard.suggestion.domain.model.keyGeometry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.abs

class AdaptiveKeysTest {

    private val qwerty = keyGeometry {
        row { "qwertyuiop".forEach { key(it, 1f) } }
        row { gap(0.5f); "asdfghjkl".forEach { key(it, 1f) }; gap(0.5f) }
        row { gap(1.5f); "zxcvbnm".forEach { key(it, 1f) }; gap(1.5f) }
    }

    // После «th» слова идут на «e», а на «w» — нет.
    private val model = modelOf("the" to 900, "then" to 700, "there" to 800, "thwart" to 50)

    @Test
    fun `у края клавиши печатается сосед, которого ждёт слово`() {
        // Нажата «w» у правого края — у самой «e».
        val adapted = resolve("th", TypedKey('w', offsetX = 0.47f))

        assertEquals('e', adapted?.char)
        // Касание пересчитано на «e»: оно у её левого края.
        assertEquals(-0.53f, adapted!!.offsetX, 0.01f)
    }

    @Test
    fun `центр клавиши — якорь, подмены нет`() {
        assertNull(resolve("th", TypedKey('w', offsetX = 0.3f)))
        assertNull(resolve("th", TypedKey('w')))
    }

    @Test
    fun `край к соседу, которого слово не ждёт, — без подмены`() {
        // Левый край «w» — у «q»: «thq» не лучше «thw».
        assertNull(resolve("th", TypedKey('w', offsetX = -0.47f)))
    }

    @Test
    fun `слабый перевес соседа подмены не даёт`() {
        val even = modelOf("abx" to 500, "abc" to 520)

        // «x» у края «c»: оба продолжения почти равны по словарю.
        assertNull(resolve("ab", TypedKey('x', offsetX = 0.47f), even))
    }

    @Test
    fun `свое слово пользователя защищает нажатую букву`() {
        val user = UserLanguageModel().apply { repeat(3) { learn("", "thwack", isSuspicious = false) } }

        val adapted = resolve("th", TypedKey('w', offsetX = 0.47f), model, user)

        // «thw» теперь начинает своё слово, но «e» всё ещё намного вероятнее — подмена остаётся.
        assertEquals('e', adapted?.char)
        assertNull(resolve("th", TypedKey('w', offsetX = 0.47f), modelOf("thwack" to 900, "the" to 100), user))
    }

    private fun resolve(
        prefix: String,
        touch: TypedKey,
        model: LanguageModel = this.model,
        user: UserLanguageModel = UserLanguageModel(),
    ): TypedKey? = AdaptiveKeys.resolve(model, user, qwerty, prefix, touch, calibrated = touch)
        ?.also { assertEquals(true, abs(it.offsetX) <= 1f) }
}
