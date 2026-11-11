package kg.timmitof.keyboard.suggestion.data

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Готовые `.trigrams` из ассетов разбираются и дают ожидаемые продолжения (пересчёт — tools/trigrams.py). */
class TrigramAssetsTest {

    @Test
    fun `русские тройки`() {
        val trigrams = load("ru_ru")

        assertTrue(trigrams.rankOf("что ты", "делаешь") in 0..2)
        assertTrue(trigrams.rankOf("спасибо за", "помощь") >= 0)
    }

    @Test
    fun `английские тройки`() {
        val trigrams = load("en_us")

        assertTrue(trigrams.rankOf("how are", "you") == 0)
        assertTrue(trigrams.rankOf("thank you", "for") >= 0)
    }

    private fun load(code: String): FollowerTable =
        FollowerTable.parse(File("src/main/assets/dictionaries/$code.trigrams").readText())
}
