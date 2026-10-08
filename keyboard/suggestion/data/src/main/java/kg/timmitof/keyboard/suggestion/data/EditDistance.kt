package kg.timmitof.keyboard.suggestion.data

import kotlin.math.abs

/**
 * Расстояние Дамерау — Левенштейна в варианте OSA: вставка, удаление, замена и перестановка соседних букв
 * стоят по одной правке. Второе слово задаётся участком строки — словарь сравнивается без подстрок.
 *
 * Строки таблицы переиспользуются между вызовами, поэтому экземпляр — на один поиск, не общий для потоков.
 */
internal class EditDistance {

    private var beforePrevious = IntArray(INITIAL_SIZE)
    private var previous = IntArray(INITIAL_SIZE)
    private var current = IntArray(INITIAL_SIZE)

    /**
     * Расстояние от [query] до `text[start, start + length)` или `max + 1`, если оно больше [max].
     * Считает только до тех пор, пока в строке таблицы есть значение не больше [max].
     */
    fun between(query: CharSequence, text: CharSequence, start: Int, length: Int, max: Int): Int {
        val limit = max + 1
        if (abs(query.length - length) > max) return limit
        if (query.isEmpty() || length == 0) return maxOf(query.length, length).coerceAtMost(limit)

        ensureCapacity(length + 1)
        for (column in 0..length) previous[column] = column

        for (row in 1..query.length) {
            val char = query[row - 1]
            current[0] = row
            var rowMin = row

            for (column in 1..length) {
                val other = text[start + column - 1]
                var value = minOf(
                    previous[column] + 1,
                    current[column - 1] + 1,
                    previous[column - 1] + if (char == other) 0 else 1,
                )
                if (row > 1 && column > 1 && char == text[start + column - 2] && query[row - 2] == other) {
                    value = minOf(value, beforePrevious[column - 2] + 1)
                }
                current[column] = value
                if (value < rowMin) rowMin = value
            }

            // Перестановка смотрит на две строки назад, но и она не опускается ниже минимума текущей строки.
            if (rowMin > max) return limit

            val recycled = beforePrevious
            beforePrevious = previous
            previous = current
            current = recycled
        }

        return previous[length].coerceAtMost(limit)
    }

    /**
     * Отличается ли начало [word] от [query] не больше чем на одну правку: сравниваются начала длиной
     * `query.length - 1 .. query.length + 1`. Точное совпадение начала тоже подходит — отсеивает вызывающий.
     */
    fun isPrefixWithinOneEdit(query: CharSequence, word: CharSequence, start: Int = 0, length: Int = word.length): Boolean {
        val from = maxOf(1, query.length - 1)
        val to = minOf(length, query.length + 1)
        for (prefix in from..to) {
            if (between(query, word, start, prefix, max = 1) <= 1) return true
        }
        return false
    }

    private fun ensureCapacity(size: Int) {
        if (previous.size >= size) return
        val capacity = maxOf(size, previous.size * 2)
        beforePrevious = IntArray(capacity)
        previous = IntArray(capacity)
        current = IntArray(capacity)
    }

    private companion object {
        const val INITIAL_SIZE = 32
    }
}
