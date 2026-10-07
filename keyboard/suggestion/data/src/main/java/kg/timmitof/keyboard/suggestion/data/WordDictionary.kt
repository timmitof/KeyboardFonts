package kg.timmitof.keyboard.suggestion.data

/**
 * Компактный словарь: слова одной строкой через `\n`, бинарный поиск по отсортированным позициям.
 * Сравнение идёт по региону строки без подстрок — подсказки пересчитываются на каждое нажатие.
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

    fun indexOf(word: String): Int {
        val index = lowerBound(word)
        return if (index < size && compareAt(index, word) == 0) index else -1
    }

    fun contains(word: String): Boolean = indexOf(word) >= 0

    fun scoreOf(word: String): Int = indexOf(word).takeIf { it >= 0 }?.let(scores::get) ?: 0

    fun prefixRange(prefix: String): IntRange {
        val from = lowerBound(prefix)
        var to = from
        while (to < size && startsWith(to, prefix)) to++
        return from until to
    }

    private fun endOf(index: Int): Int = starts[index + 1] - 1

    private fun startsWith(index: Int, prefix: String): Boolean {
        if (lengthAt(index) < prefix.length) return false
        return words.startsWith(prefix, starts[index])
    }

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

        /** Формат ассета: `слово<TAB>частота`, отсортирован. Разбор вручную без `split` — это задержка до первой подсказки. */
        fun parse(text: String): WordDictionary {
            val words = StringBuilder(text.length)
            var starts = IntArray(INITIAL_CAPACITY)
            var scores = IntArray(INITIAL_CAPACITY)
            var count = 0

            var lineStart = 0
            while (lineStart < text.length) {
                var lineEnd = text.indexOf('\n', lineStart)
                if (lineEnd < 0) lineEnd = text.length
                val contentEnd = text.lineContentEnd(lineStart, lineEnd)

                val separator = text.indexOf('\t', lineStart)
                if (separator in (lineStart + 1) until contentEnd) {
                    val score = text.parseScore(separator + 1, contentEnd)
                    if (score > 0) {
                        if (count == starts.size) {
                            starts = starts.copyOf(count * 2)
                            scores = scores.copyOf(count * 2)
                        }
                        starts[count] = words.length
                        scores[count] = score
                        count++
                        words.append(text, lineStart, separator).append('\n')
                    }
                }
                lineStart = lineEnd + 1
            }

            val bounds = starts.copyOf(count + 1)
            bounds[count] = words.length

            return WordDictionary(
                words = words.toString(),
                starts = bounds,
                scores = scores.copyOf(count),
            )
        }

        private fun String.parseScore(from: Int, to: Int): Int {
            var value = 0
            for (index in from until to) {
                val digit = this[index] - '0'
                if (digit !in 0..9) return 0
                value = value * 10 + digit
            }
            return value
        }

        val Empty = WordDictionary("", intArrayOf(0), IntArray(0))

        private const val INITIAL_CAPACITY = 8 * 1024
    }
}
