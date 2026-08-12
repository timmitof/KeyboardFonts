package kg.timmitof.keyboard.data.suggestion

/**
 * Словарь языка в компактном виде: все слова лежат в одной строке через `\n`,
 * позиции — в [starts], частоты — в [scores].
 *
 * Так словарь на 40 000 слов не превращается в 40 000 объектов `String`, а поиск
 * по префиксу сводится к бинарному поиску по отсортированному массиву.
 * Сравнения идут прямо по региону строки, без выделения подстрок —
 * подсказки пересчитываются на каждое нажатие, и мусор здесь недопустим.
 *
 * @param words слова через `\n`, отсортированные лексикографически.
 * @param starts начало каждого слова в [words]; размер — `size + 1`.
 * @param scores логарифмическая частота слова (1..1000).
 */
internal class WordDictionary(
    private val words: String,
    private val starts: IntArray,
    private val scores: IntArray,
) {

    val size: Int get() = scores.size

    fun wordAt(index: Int): String = words.substring(starts[index], endOf(index))

    fun scoreAt(index: Int): Int = scores[index]

    fun lengthAt(index: Int): Int = endOf(index) - starts[index]

    fun charAt(index: Int, offset: Int): Char = words[starts[index] + offset]

    /** Индекс слова или -1, если слова нет в словаре. */
    fun indexOf(word: String): Int {
        val index = lowerBound(word)
        return if (index < size && compareAt(index, word) == 0) index else -1
    }

    fun contains(word: String): Boolean = indexOf(word) >= 0

    fun scoreOf(word: String): Int = indexOf(word).takeIf { it >= 0 }?.let(scores::get) ?: 0

    /**
     * Диапазон слов, начинающихся с [prefix] (пустой, если таких нет).
     */
    fun prefixRange(prefix: String): IntRange {
        val from = lowerBound(prefix)
        var to = from
        while (to < size && startsWith(to, prefix)) to++
        return from until to
    }

    /**
     * Диапазон слов, начинающихся с буквы [char].
     */
    fun rangeOf(char: Char): IntRange {
        val from = lowerBound(char.toString())
        var to = from
        while (to < size && charAt(to, 0) == char) to++
        return from until to
    }

    private fun endOf(index: Int): Int = starts[index + 1] - 1

    private fun startsWith(index: Int, prefix: String): Boolean {
        if (lengthAt(index) < prefix.length) return false
        return words.startsWith(prefix, starts[index])
    }

    /** Первое слово, которое не меньше [word]. */
    private fun lowerBound(word: String): Int {
        var low = 0
        var high = size
        while (low < high) {
            val middle = (low + high) ushr 1
            if (compareAt(middle, word) < 0) low = middle + 1 else high = middle
        }
        return low
    }

    private fun compareAt(index: Int, word: String): Int {
        val start = starts[index]
        val length = lengthAt(index)
        val common = minOf(length, word.length)
        for (offset in 0 until common) {
            val diff = words[start + offset] - word[offset]
            if (diff != 0) return diff
        }
        return length - word.length
    }

    companion object {

        /**
         * Разбирает ассет формата `слово<TAB>частота` (уже отсортированный).
         */
        fun parse(text: String): WordDictionary {
            val words = StringBuilder(text.length)
            val starts = ArrayList<Int>(INITIAL_CAPACITY)
            val scores = ArrayList<Int>(INITIAL_CAPACITY)

            text.lineSequence().forEach { line ->
                val separator = line.indexOf('\t')
                if (separator <= 0) return@forEach
                val score = line.substring(separator + 1).trim().toIntOrNull() ?: return@forEach

                starts += words.length
                words.append(line, 0, separator).append('\n')
                scores += score
            }
            // Замыкающая граница: конец последнего слова + разделитель.
            starts += words.length

            return WordDictionary(
                words = words.toString(),
                starts = starts.toIntArray(),
                scores = scores.toIntArray(),
            )
        }

        val Empty = WordDictionary("", intArrayOf(0), IntArray(0))

        private const val INITIAL_CAPACITY = 48 * 1024
    }
}
