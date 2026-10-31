package kg.timmitof.keyboard.suggestion.data

/** Отбор лучших слов без выделения памяти: кандидатов на нажатие тысячи, поэтому только индексы и очки. */
internal class TopIndices(private val capacity: Int) {

    private val indices = IntArray(capacity)

    private val scores = IntArray(capacity)

    private var size = 0

    fun offer(index: Int, score: Int) {
        if (size == capacity && score <= scores[size - 1]) return

        var position = size.coerceAtMost(capacity - 1)
        while (position > 0 && scores[position - 1] < score) {
            indices[position] = indices[position - 1]
            scores[position] = scores[position - 1]
            position--
        }
        indices[position] = index
        scores[position] = score

        if (size < capacity) size++
    }

    fun indices(): IntArray = indices.copyOf(size)
}
