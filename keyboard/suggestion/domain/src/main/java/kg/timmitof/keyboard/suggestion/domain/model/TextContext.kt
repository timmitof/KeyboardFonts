package kg.timmitof.keyboard.suggestion.domain.model

/**
 * Снимок текста вокруг курсора — вход для подсказок. Текст может быть стилизован Unicode-шрифтом,
 * поэтому границы слов ищутся по разделителям, а глифы расшифровывает data-слой.
 */
data class TextContext(
    val before: String = "",
    val after: String = "",
) {

    // Производные значения считаются один раз на снимок и не входят в equals/hashCode.
    val composingWord: String by lazy(LazyThreadSafetyMode.NONE) {
        if (after.firstOrNull()?.let(::isWordChar) == true) {
            ""
        } else {
            before.takeLastWhile { isWordChar(it) }
        }
    }

    private val beforeWord: String by lazy(LazyThreadSafetyMode.NONE) {
        before.dropLast(composingWord.length)
    }

    /** Левая часть биграммы. После конца предложения пусто. */
    val previousWord: String by lazy(LazyThreadSafetyMode.NONE) {
        if (isSentenceStart) {
            ""
        } else {
            beforeWord.trimEnd { !isWordChar(it) }.takeLastWhile { isWordChar(it) }
        }
    }

    /** Отступы обрезаются, перевод строки — нет: он сам конец предложения. */
    val isSentenceStart: Boolean by lazy(LazyThreadSafetyMode.NONE) {
        val text = beforeWord.trimEnd { it in INDENTS }
        text.isEmpty() || text.last() in SENTENCE_END
    }

    val surroundingWords: List<String> by lazy(LazyThreadSafetyMode.NONE) {
        (beforeWord + ' ' + after)
            .split(*SEPARATOR_CHARS)
            .filter { it.length >= MIN_CONTEXT_WORD }
    }

    /** Обновляет снимок сразу, не дожидаясь поля ввода, иначе подсказки и авто-Shift считаются по устаревшему тексту. */
    fun appending(text: String): TextContext =
        copy(before = (before + text).takeLast(MAX_BEFORE_LENGTH))

    fun removingLast(count: Int): TextContext =
        copy(before = before.dropLast(count))

    companion object {
        const val MAX_BEFORE_LENGTH = 512

        /** Символы, которые не входят в слово. Апостроф не разделитель: `don't` — одно слово. */
        private const val SEPARATORS = ".,!?;:()[]{}<>\"«»„“”…—–-/\\|@#\$%^&*+=~`№ "

        private val SEPARATOR_CHARS = (SEPARATORS + " \t\n\r").toCharArray()

        private const val SENTENCE_END = ".!?…\n"

        private const val INDENTS = " \t"

        private const val MIN_CONTEXT_WORD = 2

        /** Узкий неразрывный пробел «Разрядки» (`StyledText.LETTER_SPACING`) — часть буквы, а не граница слова. */
        private const val LETTER_SPACING = ' '

        fun isWordChar(char: Char): Boolean =
            char == LETTER_SPACING || (!char.isWhitespace() && char !in SEPARATORS)
    }
}
