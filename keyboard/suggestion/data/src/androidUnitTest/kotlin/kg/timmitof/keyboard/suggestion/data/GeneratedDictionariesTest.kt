package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.keyboard.data.MappedBytes
import java.io.File
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Настоящие словари из сборки (`generateDictionaries`): формат файлов читается общим кодом. */
class GeneratedDictionariesTest {

    private val directory = File("build/generated/dictionaries/dictionaries")

    private val dictionary by lazy { WordDictionary.parse(File(directory, "ru_ru.dict").readText()) }

    @Test
    fun `индекс опечаток открывается поверх файла и находит слово`() {
        val bytes = MappedBytes(ByteBuffer.wrap(File(directory, "ru_ru.spell").readBytes()))
        val index = assertNotNull(DictionarySpellIndex.open(bytes, dictionary))

        assertTrue(index.corrections(query = "пирвет", maxDistance = 1).any { it.word == "привет" })
    }

    @Test
    fun `фильтр словоформ разбирается`() {
        val forms = WordForms.parse(File(directory, "ru_ru.forms").readBytes())

        assertTrue(forms.mightContain("привет"))
        assertFalse(forms === WordForms.Empty)
    }
}
