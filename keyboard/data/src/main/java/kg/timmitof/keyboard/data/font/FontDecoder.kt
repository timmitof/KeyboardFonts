package kg.timmitof.keyboard.data.font

/**
 * Возвращает стилизованный текст к обычным буквам.
 */
internal object FontDecoder {

    /** Код-поинт глифа → обычный символ. */
    private val plainByGlyph: Map<Int, Char> by lazy { buildIndex() }

    /**
     * Убирает стилизацию: глифы шрифтов заменяются обычными буквами,
     * комбинируемые знаки (подчёркивание, зачёркивание) выбрасываются.
     */
    fun decode(text: String): String {
        if (text.isEmpty() || text.none { it.needsDecoding() }) return text

        val result = StringBuilder(text.length)
        var index = 0
        while (index < text.length) {
            val codePoint = text.codePointAt(index)
            val width = Character.charCount(codePoint)

            when {
                codePoint.isCombiningMark() -> Unit
                else -> result.append(plainByGlyph[codePoint] ?: text.substring(index, index + width))
            }
            index += width
        }
        return result.toString()
    }

    /** Обычная латиница, кириллица и цифры декодирования не требуют. */
    private fun Char.needsDecoding(): Boolean = code > MAX_PLAIN_CODE

    private fun Int.isCombiningMark(): Boolean =
        Character.getType(this).let {
            it == Character.NON_SPACING_MARK.toInt() || it == Character.ENCLOSING_MARK.toInt()
        }

    /**
     * Строит индекс по всем шрифтам каталога.
     */
    private fun buildIndex(): Map<Int, Char> {
        val index = HashMap<Int, Char>(1024)

        FontCatalog.fonts.forEach { font ->
            font.charMap.forEach { (plain, glyph) ->
                val codePoint = glyph.codePointAt(0)
                if (codePoint == plain.code) return@forEach

                val current = index[codePoint]
                if (current == null || (current.isUpperCase() && plain.isLowerCase())) {
                    index[codePoint] = plain
                }
            }
        }
        return index
    }

    /** Выше этого кода начинаются стилизованные глифы и комбинируемые знаки. */
    private const val MAX_PLAIN_CODE = 0x04FF
}
