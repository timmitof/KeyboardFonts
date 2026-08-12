package kg.timmitof.keyboard.domain.model

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
        get() = if (after.firstOrNull()?.isWordChar() == true) {
            ""
        } else {
            before.takeLastWhile { it.isWordChar() }
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
            beforeWord.trimEnd { !it.isWordChar() }.takeLastWhile { it.isWordChar() }
        }

    /** Курсор стоит в начале предложения — с заглавной буквы. */
    val isSentenceStart: Boolean
        get() {
            val text = beforeWord.trimEnd { it.isWhitespace() }
            return text.isEmpty() || text.last() in SENTENCE_END
        }

    /** Слова, уже написанные в этом поле — лексика текущего диалога. */
    val surroundingWords: List<String>
        get() = (beforeWord + ' ' + after)
            .split(*SEPARATOR_CHARS)
            .filter { it.length >= MIN_CONTEXT_WORD }

    companion object {
        /** Символы, которые не входят в слово. Апостроф не разделитель: `don't` — одно слово. */
        private const val SEPARATORS = ".,!?;:()[]{}<>\"«»„“”…—–-/\\|@#\$%^&*+=~`№ "

        private val SEPARATOR_CHARS = (SEPARATORS + " \t\n\r").toCharArray()

        private const val SENTENCE_END = ".!?…\n"

        private const val MIN_CONTEXT_WORD = 2

        private fun Char.isWordChar(): Boolean = !isWhitespace() && this !in SEPARATORS
    }
}
