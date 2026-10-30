package kg.timmitof.keyboard.font.domain.model

/** Правила стилизованного текста, общие для ввода, удаления и расшифровки. */
object StyledText {

    /** Узкий неразрывный пробел после буквы в «Разрядке»: не пробел для слова, а часть буквы. */
    const val LETTER_SPACING = ' '

    /** Хвост буквы: комбинируемые знаки стиля и разрядка. Сами по себе они не стираются. */
    fun isLetterTail(char: Char): Boolean =
        char == LETTER_SPACING || when (char.category) {
            CharCategory.NON_SPACING_MARK, CharCategory.ENCLOSING_MARK -> true
            else -> false
        }

    /** Длина последнего символа вместе с хвостом: Backspace стирает «з̶» или «З » целиком. */
    fun lastSymbolLength(text: CharSequence): Int {
        var end = text.length
        while (end > 0 && isLetterTail(text[end - 1])) end--
        if (end == 0) return text.length

        val isSurrogatePair = end >= 2 && text[end - 1].isLowSurrogate() && text[end - 2].isHighSurrogate()
        return text.length - end + if (isSurrogatePair) 2 else 1
    }
}
