package kg.timmitof.keyboard.translate.data

import kg.timmitof.keyboard.translate.data.text.TranslationMemory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TranslationMemoryTest {

    private fun key(text: String) = TranslationMemory.Key("ru", "en", text)

    @Test
    fun `вытесняет давно не использованное`() {
        val memory = TranslationMemory(capacity = 2)
        memory[key("а")] = "a"
        memory[key("б")] = "b"
        memory[key("а")]
        memory[key("в")] = "v"

        assertEquals("a", memory[key("а")])
        assertNull(memory[key("б")])
        assertEquals("v", memory[key("в")])
    }

    @Test
    fun `пары языков не смешиваются`() {
        val memory = TranslationMemory(capacity = 4)
        memory[TranslationMemory.Key("ru", "en", "да")] = "yes"

        assertNull(memory[TranslationMemory.Key("ru", "de", "да")])
    }
}
