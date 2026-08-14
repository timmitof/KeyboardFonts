package kg.timmitof.keyboard.data.suggestion

import kg.timmitof.keyboard.domain.model.SuggestionRequest
import kg.timmitof.keyboard.domain.model.TextContext
import kg.timmitof.keyboard.domain.model.WordSuggestion
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Т9 на настоящих ассетах: маленький словарь в тестах доказывает логику,
 * а живые сценарии переписки — что данных для неё хватает.
 */
class RealDictionaryTest {

    private val engine = SuggestionEngine()

    private val russian by lazy { modelOf("ru_ru") }

    private val english by lazy { modelOf("en_us") }

    @Test
    fun `начатое приветствие дописывается целиком`() {
        val suggestions = suggest("Прив", russian)

        assertEquals("Привет", suggestions[1].text)
        assertTrue(suggestions[1].isAutoCorrect)
    }

    @Test
    fun `после «как» одна буква превращается в «дела»`() {
        val suggestions = suggest("Привет, как д", russian)

        assertEquals("дела", suggestions[1].text)
        assertTrue(suggestions[1].isAutoCorrect)
    }

    @Test
    fun `после «что» побеждает глагол, а не частотные «да» и «для»`() {
        val suggestions = suggest("что д", russian)

        assertTrue(suggestions[1].text.startsWith("дела"))
    }

    @Test
    fun `предсказание следующего слова опирается на живую речь`() {
        assertTrue(suggest("привет ", russian).isNotEmpty())
        assertTrue(suggest("как ", russian).isNotEmpty())
    }

    @Test
    fun `английский словарь ведёт себя так же`() {
        assertEquals("are", suggest("how a", english)[1].text)
        assertEquals("love", suggest("i lo", english)[1].text)
    }

    @Test
    fun `знакомое слово не подменяется дополнением`() {
        listOf("спас", "thank").zip(listOf(russian, english)).forEach { (typed, model) ->
            assertTrue(suggest(typed, model).none(WordSuggestion::isAutoCorrect))
        }
    }

    private fun suggest(before: String, model: LanguageModel): List<WordSuggestion> = runBlocking {
        engine.suggest(
            request = SuggestionRequest(languageCode = "test", context = TextContext(before = before)),
            model = model,
            user = UserLanguageModel(),
        )
    }

    private fun modelOf(code: String): LanguageModel {
        val assets = File("src/main/assets/dictionaries")
        return LanguageModel(
            dictionary = WordDictionary.parse(File(assets, "$code.dict").readText()),
            bigrams = BigramTable.parse(File(assets, "$code.bigrams").readText()),
        )
    }
}
