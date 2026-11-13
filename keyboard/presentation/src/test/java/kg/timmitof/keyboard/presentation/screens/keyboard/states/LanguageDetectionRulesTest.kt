package kg.timmitof.keyboard.presentation.screens.keyboard.states

import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.translate.domain.model.DetectedLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageDetectionRulesTest {

    private val russian = KeyboardLanguage(code = "ru_ru", displayName = "Русский", shortName = "RU")
    private val kyrgyz = KeyboardLanguage(code = "ky_kg", displayName = "Кыргызча", shortName = "KY")
    private val english = KeyboardLanguage(code = "en_us", displayName = "English", shortName = "EN", isLatin = true)
    private val uzbekLatin = KeyboardLanguage(code = "uz_latn", displayName = "Oʻzbekcha", shortName = "UZ", isLatin = true)
    private val uzbekCyrillic = KeyboardLanguage(code = "uz_cyrl", displayName = "Ўзбекча", shortName = "ЎЗ")

    private val enabled = listOf(russian, kyrgyz, english)

    @Test
    fun `при паре RU и EN подсказывать нечего — на русской раскладке латиницей не напечатать`() {
        assertFalse(LanguageDetectionRules.canSuggestLayout(russian, listOf(russian, english)))
        assertTrue(LanguageDetectionRules.canSuggestLayout(russian, listOf(russian, kyrgyz, english)))
    }

    @Test
    fun `короткий текст не определяем`() {
        assertFalse(LanguageDetectionRules.isLongEnough("Привет"))
        assertFalse(LanguageDetectionRules.isLongEnough("ок да"))
        assertTrue(LanguageDetectionRules.isLongEnough("Кандайсың, качан жолугабыз"))
    }

    @Test
    fun `уверенно другой включённый язык — подсказываем его раскладку`() {
        val detected = listOf(DetectedLanguage("ky", 0.82f), DetectedLanguage("ru", 0.1f))

        assertEquals(kyrgyz, suggest(detected, current = russian))
    }

    @Test
    fun `текущий язык тоже вероятен — молчим`() {
        val detected = listOf(DetectedLanguage("ky", 0.55f), DetectedLanguage("ru", 0.4f))

        assertNull(suggest(detected, current = russian))
    }

    @Test
    fun `язык не из включённых раскладок не подсказываем`() {
        val detected = listOf(DetectedLanguage("kk", 0.9f))

        assertNull(suggest(detected, current = russian))
    }

    @Test
    fun `мало уверенности — молчим`() {
        val detected = listOf(DetectedLanguage("ky", 0.4f))

        assertNull(suggest(detected, current = russian))
    }

    @Test
    fun `из двух раскладок языка — по алфавиту текста`() {
        val detected = listOf(DetectedLanguage("uz", 0.9f))
        val layouts = listOf(russian, uzbekLatin, uzbekCyrillic)

        assertEquals(uzbekLatin, LanguageDetectionRules.suggestLayout(detected, russian, layouts, "Salom, qalaysan doʻstim"))
        assertEquals(uzbekCyrillic, LanguageDetectionRules.suggestLayout(detected, russian, layouts, "Салом, қалайсан дўстим"))
    }

    @Test
    fun `автоопределение берёт самый вероятный только с запасом уверенности`() {
        assertEquals("de", LanguageDetectionRules.confidentCode(listOf(DetectedLanguage("de", 0.7f))))
        assertNull(LanguageDetectionRules.confidentCode(listOf(DetectedLanguage("de", 0.3f))))
    }

    private fun suggest(detected: List<DetectedLanguage>, current: KeyboardLanguage) =
        LanguageDetectionRules.suggestLayout(detected, current, enabled, "достаточно длинный текст")
}
