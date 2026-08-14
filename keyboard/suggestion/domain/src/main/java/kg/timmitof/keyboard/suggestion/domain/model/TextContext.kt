package kg.timmitof.keyboard.suggestion.domain.model

/**
 * Снимок текста вокруг курсора — единственный вход для подсказок Т9.
 *
 * Текст в поле может быть стилизован Unicode-шрифтом, поэтому здесь нет
 * ничего, что зависит от алфавита: границы слов ищутся по разделителям,
 * а расшифровкой глифов занимается data-слой.
 *
 * @param before текст перед курсором (ограниченное окно, а не всё поле).
 * @param after текст после курсора.
 */
data class TextContext(
    val before: String = "",
    val after: String = "",
) {

    /** Слово, которое набирается прямо сейчас. Пусто, если курсор не в конце слова. */
    val composingWord: String
        get() = if (after.firstOrNull()?.let(::isWordChar) == true) {
            ""
        } else {
            before.takeLastWhile { isWordChar(it) }
        }

    /** Текст до начала набираемого слова — на нём считается контекст предложения. */
    private val beforeWord: String
        get() = before.dropLast(composingWord.length)

    /**
     * Законченное слово перед набираемым — левая часть биграммы.
     *
     * После конца предложения контекст обрывается: подсказывать продолжение
     * предыдущей фразы бессмысленно.
     */
    val previousWord: String
        get() = if (isSentenceStart) {
            ""
        } else {
            beforeWord.trimEnd { !isWordChar(it) }.takeLastWhile { isWordChar(it) }
        }

    /**
     * Слово в этой позиции начинает предложение — его пишут с заглавной буквы.
     *
     * Отступы обрезаются, но перевод строки — нет: он сам по себе конец предложения.
     */
    val isSentenceStart: Boolean
        get() {
            val text = beforeWord.trimEnd { it in INDENTS }
            return text.isEmpty() || text.last() in SENTENCE_END
        }

    /** Слова, уже написанные в этом поле — лексика текущего диалога. */
    val surroundingWords: List<String>
        get() = (beforeWord + ' ' + after)
            .split(*SEPARATOR_CHARS)
            .filter { it.length >= MIN_CONTEXT_WORD }

    /**
     * Контекст после ввода [text].
     *
     * Клавиатура знает, что сама только что напечатала, и обновляет снимок сразу —
     * не дожидаясь ответа поля ввода. Иначе подсказки и авто-Shift успевают
     * посчитаться по устаревшему тексту.
     */
    fun appending(text: String): TextContext =
        copy(before = (before + text).takeLast(MAX_BEFORE_LENGTH))

    /** Контекст после удаления [count] символов перед курсором. */
    fun removingLast(count: Int): TextContext =
        copy(before = before.dropLast(count))

    companion object {
        /** Столько текста перед курсором держим в снимке. */
        const val MAX_BEFORE_LENGTH = 512

        /** Символы, которые не входят в слово. Апостроф не разделитель: `don't` — одно слово. */
        private const val SEPARATORS = ".,!?;:()[]{}<>\"«»„“”…—–-/\\|@#\$%^&*+=~`№ "

        private val SEPARATOR_CHARS = (SEPARATORS + " \t\n\r").toCharArray()

        private const val SENTENCE_END = ".!?…\n"

        /** Отступы внутри строки: их обрезаем, а перевод строки — нет. */
        private const val INDENTS = " \t"

        private const val MIN_CONTEXT_WORD = 2

        /**
         * Входит ли символ в слово.
         */
        fun isWordChar(char: Char): Boolean = !char.isWhitespace() && char !in SEPARATORS
    }
}
