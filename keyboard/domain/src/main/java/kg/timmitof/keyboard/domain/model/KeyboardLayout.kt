package kg.timmitof.keyboard.domain.model

data class KeyboardLayout(
    val name: String,
    val isLatin: Boolean = false,
    val largeLabels: Boolean = false,
    val rows: List<List<KeyboardKey>>
) {

    private val columns: Float
        get() = rows.firstOrNull()?.sumOf { it.weight.toDouble() }?.toFloat() ?: 0f

    val hasSubLabels: Boolean by lazy {
        rows.any { row ->
            row.any { it is KeyboardKey.Character && it.subLabel != null }
        }
    }

    fun withDigitsRow(): KeyboardLayout {
        if (rows.isEmpty() || columns <= 0f) return this

        val weight = columns / DIGITS.length
        val digits = DIGITS.map { digit ->
            KeyboardKey.Character(
                weight = weight,
                labelLower = digit.toString(),
                labelUpper = digit.toString(),
            )
        }

        return copy(rows = listOf(digits) + rows)
    }

    fun withBottomRow(bottomRow: List<KeyboardKey>): KeyboardLayout {
        if (bottomRow.isEmpty() || rows.isEmpty()) return this

        val rowWeight = bottomRow.sumOf { it.weight.toDouble() }.toFloat()
        if (rowWeight <= 0f || columns <= 0f) return this

        val factor = columns / rowWeight
        val scaled = bottomRow.map { it.withWeight(it.weight * factor) }

        return copy(rows = rows.dropLast(1) + listOf(scaled))
    }

    private companion object {
        const val DIGITS = "1234567890"
    }
}
