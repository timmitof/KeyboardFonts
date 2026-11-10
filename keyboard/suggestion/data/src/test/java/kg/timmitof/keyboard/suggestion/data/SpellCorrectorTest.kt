package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.keyboard.suggestion.domain.model.SuggestionRequest
import kg.timmitof.keyboard.suggestion.domain.model.TextContext
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpellCorrectorTest {

    @Test
    fun `находит переставленные буквы без перебора всего словаря`() {
        val corrector = correctorOf(dictionaryOf("the" to 100))

        val correction = corrector.corrections(query = "teh", maxDistance = 1).single()

        assertEquals("the", correction.word)
        assertEquals(1, correction.distance)
    }

    @Test
    fun `передает исправление в автокоррекцию`() = runBlocking {
        val model = modelOf("the" to 100)

        val suggestions = SuggestionEngine().suggest(
            request = SuggestionRequest(
                languageCode = "en_us",
                context = TextContext(before = "teh"),
            ),
            model = model,
            user = UserLanguageModel(),
        )

        assertEquals("teh", suggestions.first().text)
        assertTrue(suggestions.any { it.text == "the" && it.isAutoCorrect })
    }

    /** Файл индекса собирает build-logic, поэтому слова словаря кладём в индекс в памяти с той же логикой поиска. */
}
