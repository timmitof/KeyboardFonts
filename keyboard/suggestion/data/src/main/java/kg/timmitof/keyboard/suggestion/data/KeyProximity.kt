package kg.timmitof.keyboard.suggestion.data

internal object KeyProximity {

    private val LAYOUTS = listOf(
        listOf("qwertyuiop", "asdfghjkl", "zxcvbnm"),
        listOf("йцукенгшщзх", "фывапролджэ", "ячсмитьбю"),
    )

    private val neighbors: Map<Char, Set<Char>> = buildNeighbors()

    fun areAdjacent(first: Char, second: Char): Boolean =
        first != second && neighbors[first]?.contains(second) == true

    fun withNeighbors(char: Char): Set<Char> =
        neighbors[char]?.let { it + char } ?: setOf(char)

    private fun buildNeighbors(): Map<Char, Set<Char>> {
        val result = HashMap<Char, MutableSet<Char>>(96)

        fun link(first: Char, second: Char) {
            result.getOrPut(first) { HashSet(8) } += second
            result.getOrPut(second) { HashSet(8) } += first
        }

        LAYOUTS.forEach { rows ->
            rows.forEach { row ->
                row.forEachIndexed { index, char ->
                    if (index > 0) link(char, row[index - 1])
                }
            }
            // Ряды разной длины: соседа сверху ищем по относительной позиции.
            rows.zipWithNext { upper, lower ->
                lower.forEachIndexed { index, char ->
                    val center = (index * upper.length) / lower.length
                    (center..center + 1).forEach { neighbor ->
                        upper.getOrNull(neighbor)?.let { link(char, it) }
                    }
                }
            }
        }
        return result
    }
}
