package kg.timmitof.keyboard.data.suggestion

/**
 * Статистика пар слов языка: что обычно идёт после слова.
 *
 * Даёт клавиатуре предсказание следующего слова ещё до того,
 * как она чему-то научилась у пользователя.
 *
 * Хранится компактно, как и словарь: все продолжения лежат в одной строке
 * блоками через пробел, а [blocks] переводит первое слово в номер блока.
 * Иначе полсотни тысяч пар превратились бы в сотни тысяч мелких объектов —
 * при том, что за один запрос читается один блок.
 *
 * @param blocks первое слово пары → номер его блока продолжений.
 * @param bounds границы блоков в [followers]; размер — `blocks.size + 1`.
 * @param followers продолжения через пробел, блоки идут подряд и внутри
 * отсортированы по убыванию частоты.
 */
internal class BigramTable(
    private val blocks: Map<String, Int>,
    private val bounds: IntArray,
    private val followers: String,
) {

    /**
     * @param word слово-продолжение.
     * @param score сила связи (100..1000), выведенная из места в блоке.
     */
    class Follower(val word: String, val score: Int)

    /** Продолжения слова, самые частые первыми. */
    fun after(word: String, limit: Int = Int.MAX_VALUE): List<Follower> =
        collect(word, limit) { _, _ -> true }

    /** Продолжения, начинающиеся с [prefix] — то, что человек уже начал набирать. */
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

    /** Обходит блок слова, отдавая границы каждого продолжения и его место в блоке. */
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

        /**
         * Разбирает ассет формата `слово<TAB>продолжение продолжение ...`,
         * где продолжения отсортированы по убыванию частоты.
         */
        fun parse(text: String): BigramTable {
            val blocks = HashMap<String, Int>(INITIAL_CAPACITY)
            var bounds = IntArray(INITIAL_CAPACITY)
            val followers = StringBuilder(text.length)

            var lineStart = 0
            while (lineStart < text.length) {
                var lineEnd = text.indexOf('\n', lineStart)
                if (lineEnd < 0) lineEnd = text.length

                val separator = text.indexOf('\t', lineStart)
                if (separator in (lineStart + 1) until lineEnd) {
                    val block = blocks.size
                    if (block == bounds.size) bounds = bounds.copyOf(block * 2)
                    bounds[block] = followers.length
                    blocks[text.substring(lineStart, separator)] = block

                    followers.append(text, separator + 1, lineEnd).append(' ')
                }
                lineStart = lineEnd + 1
            }

            // Замыкающая граница: конец последнего блока.
            val closed = bounds.copyOf(blocks.size + 1)
            closed[blocks.size] = followers.length

            return BigramTable(blocks, closed, followers.toString())
        }

        val Empty = BigramTable(emptyMap(), intArrayOf(0), "")

        /**
         * Сила связи по месту в блоке.
         *
         * Даже последнее продолжение остаётся весомым: раз пара попала в словарь,
         * она встречается в живой речи, и это важнее словарной частоты самого слова —
         * иначе после «как» вместо «дела» будет побеждать частотное «два».
         */
        private fun scoreOfRank(rank: Int): Int =
            maxOf(MIN_SCORE, MAX_SCORE - rank * RANK_STEP)

        private const val MAX_SCORE = 1000
        private const val MIN_SCORE = 100
        private const val RANK_STEP = 10

        private const val DEFAULT_CAPACITY = 8
        private const val INITIAL_CAPACITY = 16 * 1024
    }
}
