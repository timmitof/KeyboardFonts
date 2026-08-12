package kg.timmitof.keyboard.data.suggestion

/**
 * Статистика пар слов языка: что обычно идёт после слова.
 *
 * Даёт клавиатуре предсказание следующего слова ещё до того,
 * как она чему-то научилась у пользователя.
 */
internal class BigramTable(private val followers: Map<String, List<Follower>>) {

    /**
     * @param word слово-продолжение.
     * @param score логарифмическая частота пары (1..1000).
     */
    class Follower(val word: String, val score: Int)

    fun after(word: String): List<Follower> = followers[word].orEmpty()

    fun scoreOf(previous: String, word: String): Int =
        after(previous).firstOrNull { it.word == word }?.score ?: 0

    companion object {

        /** Разбирает ассет формата `слово<TAB>продолжение<TAB>частота`. */
        fun parse(text: String): BigramTable {
            val followers = HashMap<String, MutableList<Follower>>(4096)

            text.lineSequence().forEach { line ->
                val columns = line.split('\t')
                if (columns.size < 3) return@forEach
                val score = columns[2].trim().toIntOrNull() ?: return@forEach

                followers.getOrPut(columns[0]) { ArrayList(MAX_FOLLOWERS) } +=
                    Follower(columns[1], score)
            }
            return BigramTable(followers)
        }

        val Empty = BigramTable(emptyMap())

        private const val MAX_FOLLOWERS = 8
    }
}
