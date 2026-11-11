package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.keyboard.suggestion.domain.model.SuggestionRequest
import kg.timmitof.keyboard.suggestion.domain.model.TextContext
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class TrigramTest {

    // После «тебя» чаще всего «есть», но после «как у тебя» — «дела».
    private val model = modelOf(
        "есть" to 900, "дела" to 500, "нет" to 800,
        bigrams = "тебя\tесть нет дела\n",
        trigrams = "у тебя\tдела есть\n",
    )

    @Test
    fun `следующее слово предсказывается по двум предыдущим`() {
        assertEquals("дела", suggest("как у тебя ").first())
        assertEquals("есть", suggest("люблю тебя ").first())
    }

    @Test
    fun `тройка помогает и при дописывании слова`() {
        // «д» после «у тебя»: «дела» ждёт фраза — оно первое после набранного.
        assertEquals("дела", suggest("как у тебя д")[1])
    }

    @Test
    fun `конец предложения разрывает тройку`() {
        assertEquals("есть", suggest("я у. тебя ").first())
    }

    private fun suggest(before: String): List<String> = runBlocking {
        SuggestionEngine().suggest(
            request = SuggestionRequest(languageCode = "ru_ru", context = TextContext(before = before)),
            model = model,
            user = UserLanguageModel(),
        ).map { it.text }
    }
}
