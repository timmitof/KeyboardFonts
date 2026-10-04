package kg.timmitof.keyboard.presentation.preview

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.presentation.components.KeyRowCount
import kg.timmitof.keyboard.presentation.components.KeyRowHeight
import kg.timmitof.keyboard.presentation.components.KeyRowSpacing
import kg.timmitof.keyboard.presentation.components.TopBarHeight
import kg.timmitof.keyboard.presentation.theme.KeyboardColorScheme

/** Высота клавиатуры без системных отступов — по ней строится рамка кадрирования фото. */
fun KeyboardSettings.keyboardHeight(): Dp {
    val rows = KeyRowCount + if (isDigitsRowEnabled) 1 else 0
    return VerticalPadding * 2 + TopBarHeight + KeyRowSpacing / 2 + KeyRowHeight * height.scale * rows
}

/** Упрощённая раскладка: ряды клавиш без букв — для миниатюр и подсказки «как лягут клавиши». */
fun DrawScope.drawKeyboardSilhouette(
    colors: KeyboardColorScheme,
    padding: Dp,
    gap: Dp,
    radius: Dp,
    topInset: Dp = 0.dp,
    alpha: Float = 1f,
) {
    val paddingPx = padding.toPx()
    val gapPx = gap.toPx()
    val top = paddingPx + topInset.toPx()
    val keyHeight = (size.height - top - paddingPx - gapPx * (SilhouetteRows.size - 1)) / SilhouetteRows.size
    val column = (size.width - paddingPx * 2 - gapPx * (SilhouetteColumns - 1)) / SilhouetteColumns
    val corner = CornerRadius(radius.toPx())

    SilhouetteRows.forEachIndexed { rowIndex, row ->
        var x = paddingPx
        val y = top + rowIndex * (keyHeight + gapPx)
        row.forEach { key ->
            val width = column * key.span + gapPx * (key.span - 1)
            drawRoundRect(
                color = when (key.kind) {
                    SilhouetteKey.Kind.LETTER -> colors.keyButtonBackground
                    SilhouetteKey.Kind.SPECIAL -> colors.keySpecialButtonBackground
                    SilhouetteKey.Kind.ENTER -> colors.keyEnterBackground
                },
                topLeft = Offset(x, y),
                size = Size(width, keyHeight),
                cornerRadius = corner,
                alpha = alpha,
            )
            x += width + gapPx
        }
    }
}

private class SilhouetteKey(val span: Int, val kind: Kind) {
    enum class Kind { LETTER, SPECIAL, ENTER }
}

private const val SilhouetteColumns = 10

private val SilhouetteRows: List<List<SilhouetteKey>> = listOf(
    List(10) { SilhouetteKey(1, SilhouetteKey.Kind.LETTER) },
    List(10) { SilhouetteKey(1, SilhouetteKey.Kind.LETTER) },
    listOf(SilhouetteKey(1, SilhouetteKey.Kind.SPECIAL)) +
        List(8) { SilhouetteKey(1, SilhouetteKey.Kind.LETTER) } +
        SilhouetteKey(1, SilhouetteKey.Kind.SPECIAL),
    listOf(
        SilhouetteKey(2, SilhouetteKey.Kind.SPECIAL),
        SilhouetteKey(6, SilhouetteKey.Kind.LETTER),
        SilhouetteKey(2, SilhouetteKey.Kind.ENTER),
    ),
)

private val VerticalPadding = 8.dp
