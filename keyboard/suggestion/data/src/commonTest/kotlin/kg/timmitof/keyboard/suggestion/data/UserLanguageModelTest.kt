package kg.timmitof.keyboard.suggestion.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserLanguageModelTest {

    @Test
    fun `слова по префиксу без самого префикса`() {
        val model = UserLanguageModel()
        listOf("кот", "котик", "котлета", "кит").forEach { model.learn("", it) }

        assertEquals(listOf("котик", "котлета"), model.wordsWithPrefix("кот"))
    }

    @Test
    fun `частые слова и пары считаются`() {
        val model = UserLanguageModel()
        repeat(3) { model.learn("привет", "мир") }
        model.learn("", "пока")

        assertEquals(listOf("мир", "пока"), model.frequentWords(2))
        assertEquals(3, model.pairCount("привет", "мир"))
        assertTrue(model.knows("мир"))
        assertTrue(model.isRecent("пока"))
    }

    @Test
    fun `unlearn удаляет обнуленное слово`() {
        val model = UserLanguageModel()
        model.learn("я", "кот")
        model.unlearn("я", "кот")

        assertEquals(0, model.countOf("кот"))
        assertEquals(emptyList(), model.wordsWithPrefix("ко"))
        assertTrue(model.followersOf("я").isEmpty())
    }

    @Test
    fun `экспорт и восстановление сохраняют выученное`() {
        val model = UserLanguageModel()
        repeat(2) { model.learn("привет", "мир") }
        model.reject("првиет")
        model.learn("", "првет", isSuspicious = true)

        val restored = UserLanguageModel().apply { restore(model.export().asSequence()) }

        assertEquals(2, restored.countOf("мир"))
        assertEquals(2, restored.pairCount("привет", "мир"))
        assertTrue(restored.hasRejected("првиет"))
        assertTrue(restored.isSuspicious("првет"))
        assertFalse(restored.isSuspicious("мир"))
    }
}
