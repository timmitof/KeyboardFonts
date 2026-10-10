package kg.timmitof.keyboard.data.font

import kg.timmitof.keyboard.font.domain.model.StyledText

object FontDecoder {

    private val plainByGlyph: Map<Int, Char> by lazy { buildIndex() }

    fun decode(text: String): String {
        if (text.isEmpty() || text.none { it.needsDecoding() }) return text

        val result = StringBuilder(text.length)
        var index = 0
        while (index < text.length) {
            val codePoint = text.codePointAt(index)
            val width = Character.charCount(codePoint)

            when {
                width == 1 && StyledText.isLetterTail(text[index]) -> Unit
                else -> result.append(plainByGlyph[codePoint] ?: text.substring(index, index + width))
            }
            index += width
        }
        return result.toString()
    }

    /** Стилизованный глиф, знак стиля или разрядка; суррогаты — глифы из математических блоков. */
    private fun Char.needsDecoding(): Boolean =
        isSurrogate() || StyledText.isLetterTail(this) || code in plainByGlyph

    private fun buildIndex(): Map<Int, Char> {
        val index = HashMap<Int, Char>(1024)

        FontCatalog.fonts.forEach { font ->
            font.charMap.forEach { (plain, glyph) ->
                val codePoint = glyph.codePointAt(0)
                // Обычные буквы и цифры остаются собой: «Разрядка» начинается с самой буквы, а не с глифа.
                if (codePoint == plain.code || codePoint.isPlainLetter()) return@forEach

                val current = index[codePoint]
                if (current == null || (current.isUpperCase() && plain.isLowerCase())) {
                    index[codePoint] = plain
                }
            }
        }
        return index
    }

    /** Базовая латиница, цифры и современная кириллица — настоящий текст, его не расшифровываем. */
    private fun Int.isPlainLetter(): Boolean =
        this < BASIC_LATIN_END || this in MODERN_CYRILLIC

    private const val BASIC_LATIN_END = 0x80

    private val MODERN_CYRILLIC = 0x0400..0x045F
}
