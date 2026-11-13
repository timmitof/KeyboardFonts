package kg.timmitof.keyboard.translate.data

import kg.timmitof.keyboard.translate.data.text.SentenceSplitter
import org.junit.Assert.assertEquals
import org.junit.Test

class SentenceSplitterTest {

    @Test
    fun `режет по знакам конца предложения и оставляет пробелы при куске`() {
        assertEquals(
            listOf("Привет! ", "Как дела? ", "Давай в 7"),
            SentenceSplitter.split("Привет! Как дела? Давай в 7"),
        )
    }

    @Test
    fun `склеенные куски дают исходный текст`() {
        val text = "  Да!!  Нет...\n\nНу ладно. "
        assertEquals(text, SentenceSplitter.split(text).joinToString(separator = ""))
    }

    @Test
    fun `точка внутри слова не граница`() {
        assertEquals(listOf("Сайт example.com работает"), SentenceSplitter.split("Сайт example.com работает"))
    }

    @Test
    fun `перевод строки — граница`() {
        assertEquals(listOf("Первая\n", "Вторая"), SentenceSplitter.split("Первая\nВторая"))
    }

    @Test
    fun `пустой текст — пустой список`() {
        assertEquals(emptyList<String>(), SentenceSplitter.split(""))
    }
}
