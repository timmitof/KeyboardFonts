package kg.timmitof.keyboard.data.suggestion

/**
 * Считает, насколько дорого превратить набранное [typed] в начало словарного слова.
 *
 * Это расстояние Дамерау — Левенштейна со свободным «хвостом»: слово в словаре
 * длиннее набранного, и лишние буквы в конце ничего не стоят — это дополнение,
 * а не ошибка. Правки взвешены: промах по соседней клавише дешевле остальных.
 *
 * Матрица считается полосой вокруг диагонали и переиспользуется между словами —
 * за одно нажатие матчер прогоняет тысячи слов и не должен мусорить.
 *
 * @param typed набранное слово в нижнем регистре.
 * @param maxCost бюджет правок; дороже — слово отбрасывается.
 */
internal class PrefixMatcher(
    private val typed: String,
    private val maxCost: Int,
) {

    /** На сколько букв длина слова может разойтись с набранной частью. */
    private val band = maxCost / EDIT

    private val width = typed.length + band + 2

    private val rows = Array(ROWS) { IntArray(width) }

    /**
     * Стоимость правок для слова [index] или [NO_MATCH], если она выше бюджета.
     */
    fun match(dictionary: WordDictionary, index: Int): Int {
        val typedLength = typed.length
        val wordLength = dictionary.lengthAt(index)
        if (wordLength + band < typedLength) return NO_MATCH

        val limit = minOf(wordLength, typedLength + band)
        rows.forEach { it.fill(NO_MATCH) }

        var twoBack = rows[0]
        var previous = rows[1]
        var current = rows[2]

        // Нулевая строка: пропущенные буквы слова в начале.
        for (column in 0..minOf(limit, band)) previous[column] = column * EDIT

        for (row in 1..typedLength) {
            val typedChar = typed[row - 1]
            val from = maxOf(1, row - band)
            val to = minOf(limit, row + band)

            current[from - 1] = if (from == 1) row * EDIT else NO_MATCH
            var best = current[from - 1]

            for (column in from..to) {
                val wordChar = dictionary.charAt(index, column - 1)

                val replacement = when {
                    typedChar == wordChar -> 0
                    KeyProximity.areAdjacent(typedChar, wordChar) -> ADJACENT
                    else -> EDIT
                }

                var cost = minOf(
                    previous[column - 1] + replacement,  // замена или совпадение
                    previous[column] + EDIT,             // лишняя буква в наборе
                    current[column - 1] + EDIT,          // пропущенная буква
                )

                val isSwap = row > 1 && column > 1 &&
                        typedChar == dictionary.charAt(index, column - 2) &&
                        typed[row - 2] == wordChar
                if (isSwap) cost = minOf(cost, twoBack[column - 2] + TRANSPOSE)

                current[column] = cost
                if (cost < best) best = cost
            }

            if (best > maxCost) return NO_MATCH

            val oldest = twoBack
            twoBack = previous
            previous = current
            current = oldest
            current.fill(NO_MATCH)
        }

        var result = NO_MATCH
        for (column in maxOf(0, typedLength - band)..minOf(limit, typedLength + band)) {
            if (previous[column] < result) result = previous[column]
        }
        return if (result > maxCost) NO_MATCH else result
    }

    companion object {
        /** Слово не подошло под бюджет правок. */
        const val NO_MATCH = Int.MAX_VALUE / 4

        /** Обычная правка: лишняя, пропущенная или чужая буква. */
        const val EDIT = 2

        /** Промах по соседней клавише — половина обычной правки. */
        const val ADJACENT = 1

        /** Переставленные местами буквы. */
        const val TRANSPOSE = 2

        private const val ROWS = 3

        /**
         * Бюджет правок для набранного слова: чем длиннее набор,
         * тем больше опечаток можно простить.
         */
        fun budgetFor(typedLength: Int): Int = when {
            typedLength < 3 -> ADJACENT
            typedLength < 5 -> EDIT
            typedLength < 8 -> EDIT + ADJACENT
            else -> EDIT * 2
        }
    }
}
