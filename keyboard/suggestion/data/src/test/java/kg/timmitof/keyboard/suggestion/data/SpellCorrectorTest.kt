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
        val corrector = SpellCorrector.build(dictionaryOf("the" to 100))

        val correction = corrector.corrections(query = "teh", maxDistance = 1.0).single()

        assertEquals("the", correction.word)
        assertEquals(1, correction.distance)
    }

    @Test
    fun `передает исправление в автокоррекцию`() = runBlocking {
        val dictionary = dictionaryOf("the" to 100)
        val model = LanguageModel(
            dictionary = dictionary,
            bigrams = BigramTable.Empty,
            spellCorrector = SpellCorrector.build(dictionary),
        )

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

    private fun dictionaryOf(vararg entries: Pair<String, Int>): WordDictionary =
        WordDictionary.parse(
            entries
                .sortedBy(Pair<String, Int>::first)
                .joinToString(separator = "\n") { (word, score) -> "$word\t$score" },
        )
}
