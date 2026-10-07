package kg.timmitof.keyboard.presentation.components

import androidx.compose.runtime.Immutable
import kg.timmitof.keyboard.domain.model.KeyboardKey

@Immutable
internal data class SplitRow(
    val left: List<KeyboardKey>,
    val right: List<KeyboardKey>,
)

/**
 * Делит ряды пополам по ширине. Клавиша уходит в ту половину, где её центр; пробел посередине
 * режется на два, чтобы он был под обоими большими пальцами.
 */
internal fun List<List<KeyboardKey>>.splitInHalves(): List<SplitRow> = map { row ->
    val middle = row.sumOf { it.weight.toDouble() }.toFloat() / 2
    val left = mutableListOf<KeyboardKey>()
    val right = mutableListOf<KeyboardKey>()

    var start = 0f
    row.forEach { key ->
        val end = start + key.weight
        when {
            end <= middle + Epsilon -> left += key
            start >= middle - Epsilon -> right += key
            key is KeyboardKey.Space -> {
                left += key.withWeight(middle - start)
                right += key.withWeight(end - middle)
            }
            (start + end) / 2 <= middle -> left += key
            else -> right += key
        }
        start = end
    }
    SplitRow(left = left, right = right)
}

/** Клавиша «скрыть» встаёт в конец нижнего ряда — под правый большой палец. */
internal fun List<List<KeyboardKey>>.withHideKey(): List<List<KeyboardKey>> {
    if (isEmpty()) return this
    return dropLast(1) + listOf(last() + KeyboardKey.HideKeyboard(weight = HideKeyWeight))
}

private const val Epsilon = 0.001f
private const val HideKeyWeight = 1f
