package kg.timmitof.keyboard.data.suggestion

import kg.timmitof.keyboard.data.font.FontCatalog
import kg.timmitof.keyboard.domain.model.SuggestionRequest
import kg.timmitof.keyboard.domain.model.TextContext
import kg.timmitof.keyboard.domain.model.WordSuggestion
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Поведение Т9 на маленьком словаре: дополнение, исправление опечаток,
 * предсказание следующего слова и подстройка под текст поля.
 */
class SuggestionEngineTest {

    private val engine = SuggestionEngine()

    private val model = LanguageModel(
        dictionary = dictionaryOf(
            "привет" to 900,
            "привести" to 700,
            "приветствие" to 500,
            "пирог" to 400,
            "как" to 950,
            "дела" to 800,
            "hello" to 600,
        ),
        bigrams = BigramTable.parse("привет\tкак\t900\nкак\tдела\t900\n"),
    )

    @Test
    fun `дополняет набранный префикс самым частым словом`() {
        val suggestions = suggest(before = "прив")

        assertEquals("привет", suggestions[1].text)
        assertTrue(suggestions.any { it.text == "приветствие" || it.text == "привести" })
    }

    @Test
    fun `первый слот повторяет набранное и берёт незнакомое слово в кавычки`() {
        val unknown = suggest(before = "привт")
        assertEquals("привт", unknown[0].text)
        assertTrue(unknown[0].isLiteral)

        val known = suggest(before = "привет")
        assertEquals("привет", known[0].text)
        assertFalse(known[0].isLiteral)
    }

    @Test
    fun `исправляет перестановку букв и помечает исправление автозаменой`() {
        val suggestions = suggest(before = "пирвет")

        assertEquals("привет", suggestions[1].text)
        assertTrue(suggestions[1].isAutoCorrect)
    }

    @Test
    fun `не исправляет слово, которое есть в словаре`() {
        val suggestions = suggest(before = "пирог")

        assertTrue(suggestions.none(WordSuggestion::isAutoCorrect))
    }

    @Test
    fun `подсказывает следующее слово по предыдущему`() {
        val suggestions = suggest(before = "привет ")

        assertEquals("как", suggestions.first().text)
    }

    @Test
    fun `выученная пара слов обгоняет словарную`() {
        val user = UserLanguageModel()
        repeat(5) { user.learn(previous = "привет", word = "дела") }

        val suggestions = suggest(before = "привет ", user = user)

        assertEquals("дела", suggestions.first().text)
    }

    @Test
    fun `слово из текста поля попадает в подсказки, даже если его нет в словаре`() {
        val suggestions = suggest(before = "Тимурыч уже написал: Тим")

        assertTrue(suggestions.any { it.text.lowercase() == "тимурыч" })
    }

    @Test
    fun `повторяет регистр набранного слова`() {
        assertEquals("Привет", suggest(before = "Прив")[1].text)
        assertEquals("ПРИВЕТ", suggest(before = "ПРИВ")[1].text)
    }

    @Test
    fun `в начале предложения предсказание идёт с заглавной буквы`() {
        val user = UserLanguageModel()
        repeat(3) { user.learn(previous = "", word = "привет") }

        val suggestions = suggest(before = "", user = user)

        assertEquals("Привет", suggestions.first().text)
    }

    @Test
    fun `стилизованный шрифтом текст ищется как обычный`() {
        val fraktur = FontCatalog.fonts.first { it.id == "fraktur" }

        val suggestions = suggest(before = fraktur.apply("hel"))

        assertEquals("hello", suggestions[1].text)
    }

    private fun suggest(
        before: String,
        user: UserLanguageModel = UserLanguageModel(),
    ): List<WordSuggestion> = runBlocking {
        engine.suggest(
            request = SuggestionRequest(
                languageCode = "ru_ru",
                context = TextContext(before = before),
            ),
            model = model,
            user = user,
        )
    }

    private fun dictionaryOf(vararg words: Pair<String, Int>): WordDictionary =
        WordDictionary.parse(
            words.sortedBy { it.first }.joinToString("\n") { "${it.first}\t${it.second}" }
        )
}
