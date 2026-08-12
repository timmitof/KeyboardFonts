package kg.timmitof.keyboard.domain.model

/**
 * Раскладка клавиатуры.
 *
 * @param isLatin раскладка набирает латиницей.
 * @param largeLabels (телефонная раскладка).
 */
data class KeyboardLayout(
    val name: String,
    val isLatin: Boolean = false,
    val largeLabels: Boolean = false,
    val rows: List<List<KeyboardKey>>
) {

    /** Суммарный вес ряда — «ширина» раскладки в колонках. */
    private val columns: Float
        get() = rows.firstOrNull()?.sumOf { it.weight.toDouble() }?.toFloat() ?: 0f

    /**
     * Есть ли в раскладке подписи под метками.
     */
    val hasSubLabels: Boolean by lazy {
        rows.any { row ->
            row.any { it is KeyboardKey.Character && it.subLabel != null }
        }
    }

    /**
     * Раскладка с подменённым нижним рядом.
     *
     * Ряд-вариант описывается в собственном масштабе (например, 10 колонок), поэтому
     * перед подстановкой нормируется под ширину раскладки: у русской раскладки
     * 11 колонок, и без нормировки шаг клавиш нижнего ряда разъехался бы с остальными.
     */
    fun withBottomRow(bottomRow: List<KeyboardKey>): KeyboardLayout {
        if (bottomRow.isEmpty() || rows.isEmpty()) return this

        val rowWeight = bottomRow.sumOf { it.weight.toDouble() }.toFloat()
        if (rowWeight <= 0f || columns <= 0f) return this

        val factor = columns / rowWeight
        val scaled = bottomRow.map { it.withWeight(it.weight * factor) }

        return copy(rows = rows.dropLast(1) + listOf(scaled))
    }
}
