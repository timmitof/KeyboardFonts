package kg.timmitof.keyboard.suggestion.data

/**
 * Пары слов: что обычно идёт после слова. Все продолжения лежат одной строкой
 * блоками через пробел (чтобы не плодить сотни тысяч мелких объектов).
 *
 * @param blocks первое слово пары → номер его блока.
 * @param bounds границы блоков в [followers]; размер — `blocks.size + 1`.
 * @param followers продолжения через пробел, внутри блока по убыванию частоты.
 */
internal class BigramTable(
    private val blocks: Map<String, Int>,
    private val bounds: IntArray,
    private val followers: String,
) {

    /** [score] (100..1000) выводится из места слова в блоке. */
    class Follower(val word: String, val score: Int)

    fun after(word: String, limit: Int = Int.MAX_VALUE): List<Follower> =
        collect(word, limit) { _, _ -> true }

    fun followersWithPrefix(word: String, prefix: String): List<Follower> =
        collect(word, Int.MAX_VALUE) { start, end ->
            end - start > prefix.length && followers.startsWith(prefix, start)
        }

    fun scoreOf(previous: String, word: String): Int {
        forEachFollower(previous) { start, end, rank ->
            if (end - start == word.length && followers.startsWith(word, start)) {
                return scoreOfRank(rank)
            }
        }
        return 0
    }

    private inline fun collect(
        word: String,
        limit: Int,
        accept: (start: Int, end: Int) -> Boolean,
    ): List<Follower> {
        val result = ArrayList<Follower>(minOf(limit, DEFAULT_CAPACITY))
        forEachFollower(word) { start, end, rank ->
            if (result.size >= limit) return result
            if (accept(start, end)) {
                result += Follower(followers.substring(start, end), scoreOfRank(rank))
            }
        }
        return result
    }

    private inline fun forEachFollower(word: String, action: (Int, Int, Int) -> Unit) {
        val block = blocks[word] ?: return
        val blockEnd = bounds[block + 1]
        var start = bounds[block]
        var rank = 0

        while (start < blockEnd) {
            var end = followers.indexOf(' ', start)
            if (end < 0 || end > blockEnd) end = blockEnd

            action(start, end, rank)
            rank++
            start = end + 1
        }
    }

    companion object {

        /** Формат ассета: `слово<TAB>продолжение продолжение ...`, по убыванию частоты. */
        fun parse(text: String): BigramTable {
            val blocks = HashMap<String, Int>(INITIAL_CAPACITY)
            var bounds = IntArray(INITIAL_CAPACITY)
            val followers = StringBuilder(text.length)

            var lineStart = 0
            while (lineStart < text.length) {
                var lineEnd = text.indexOf('\n', lineStart)
                if (lineEnd < 0) lineEnd = text.length
                val contentEnd = text.lineContentEnd(lineStart, lineEnd)

                val separator = text.indexOf('\t', lineStart)
                if (separator in (lineStart + 1) until contentEnd) {
                    val block = blocks.size
                    if (block == bounds.size) bounds = bounds.copyOf(block * 2)
                    bounds[block] = followers.length
                    blocks[text.substring(lineStart, separator)] = block

                    followers.append(text, separator + 1, contentEnd).append(' ')
                }
                lineStart = lineEnd + 1
            }

            val closed = bounds.copyOf(blocks.size + 1)
            closed[blocks.size] = followers.length

            return BigramTable(blocks, closed, followers.toString())
        }

        val Empty = BigramTable(emptyMap(), intArrayOf(0), "")

        /** Даже последнее продолжение весомо: пара важнее частоты слова (после «как» «дела» должно обгонять «два»). */
        private fun scoreOfRank(rank: Int): Int =
            maxOf(MIN_SCORE, MAX_SCORE - rank * RANK_STEP)

        private const val MAX_SCORE = 1000
        private const val MIN_SCORE = 100
        private const val RANK_STEP = 10

        private const val DEFAULT_CAPACITY = 8
        private const val INITIAL_CAPACITY = 16 * 1024
    }
}
