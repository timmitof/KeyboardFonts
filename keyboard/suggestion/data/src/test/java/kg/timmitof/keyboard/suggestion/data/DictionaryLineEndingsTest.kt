package kg.timmitof.keyboard.suggestion.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * На Windows git может выложить ассеты с CRLF, и они так же попадут в APK.
 * Разбор обязан давать одинаковый результат для `\n` и `\r\n`.
 */
class DictionaryLineEndingsTest {

    @Test
    fun `словарь с CRLF разбирается так же, как с LF`() {
        val lf = WordDictionary.parse(DICTIONARY)
        val crlf = WordDictionary.parse(DICTIONARY.toCrlf())

        assertEquals(4, lf.size)
        assertEquals(lf.size, crlf.size)

        for (word in listOf("cat", "catch", "dog", "the")) {
            assertTrue(crlf.contains(word))
            assertEquals(lf.scoreOf(word), crlf.scoreOf(word))
        }
        assertEquals(300, crlf.scoreOf("the"))
        assertFalse(crlf.contains("the\r"))

        assertEquals(lf.prefixRange("cat"), crlf.prefixRange("cat"))
        assertEquals(listOf("cat", "catch"), crlf.prefixRange("cat").map(crlf::wordAt))
    }

    @Test
    fun `биграммы с CRLF разбираются так же, как с LF`() {
        val lf = BigramTable.parse(BIGRAMS)
        val crlf = BigramTable.parse(BIGRAMS.toCrlf())

        for (word in listOf("a", "how")) {
            assertEquals(lf.after(word).words(), crlf.after(word).words())
        }
        // Последнее продолжение блока — именно к нему прилипал '\r'.
        assertEquals("good", crlf.after("a").last().word)
        assertEquals("you", crlf.after("how").last().word)

        assertEquals(lf.scoreOf("a", "good"), crlf.scoreOf("a", "good"))
        assertEquals(980, crlf.scoreOf("a", "good"))
        assertEquals(990, crlf.scoreOf("how", "you"))

        assertEquals(listOf("good"), crlf.followersWithPrefix("a", "go").words())
        assertEquals(lf.followersWithPrefix("how", "y").words(), crlf.followersWithPrefix("how", "y").words())
    }

    private fun List<BigramTable.Follower>.words(): List<String> = map(BigramTable.Follower::word)

    private fun String.toCrlf(): String = replace("\n", "\r\n")

    private companion object {
        const val DICTIONARY = "cat\t120\ncatch\t80\ndog\t95\nthe\t300\n"
        const val BIGRAMS = "a\tlittle lot good\nhow\tare you\n"
    }
}
