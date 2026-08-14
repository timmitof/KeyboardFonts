package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.presentation.components.KeyCornerRadius
import kg.timmitof.keyboard.presentation.components.KeySupport
import kg.timmitof.keyboard.presentation.theme.KFTheme

/** Размер «шапки», которая всплывает над нажатой клавишей. */
private val PreviewWidth = 44.dp
private val PreviewHeight = 50.dp

/** Зазор между клавишей и шапкой. */
private val PreviewGap = 4.dp

private val PreviewGlyphSize = 26.sp
private val PreviewWordSize = 15.sp

/**
 * Подсказка над нажатой клавишей.
 *
 * Рисуется прямо в иерархии клавиатуры, а не в отдельном окне: `Popup` — это
 * настоящее окно системы, и создавать его на каждое нажатие клавиши слишком дорого,
 * чтобы набор оставался плавным. Чтобы шапку не перекрыл сосед справа,
 * нажатая клавиша поднимается по `zIndex`.
 */
@Composable
internal fun BoxScope.KeyPressPreview(label: String) {
    val surface = KFTheme.color.keyButtonBackground
    val support = KFTheme.color.keyButtonShadow

    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = -(PreviewHeight + PreviewGap))
            .width(PreviewWidth)
            .height(PreviewHeight)
            .drawBehind {
                val supportPx = KeySupport.toPx()
                val radius = CornerRadius(KeyCornerRadius.toPx())
                val capSize = Size(size.width, size.height - supportPx)

                drawRoundRect(
                    color = support,
                    topLeft = Offset(0f, supportPx),
                    size = capSize,
                    cornerRadius = radius
                )
                drawRoundRect(
                    color = surface,
                    topLeft = Offset.Zero,
                    size = capSize,
                    cornerRadius = radius
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = if (label.isWordLabel()) PreviewWordSize else PreviewGlyphSize,
            fontWeight = FontWeight.Medium,
            color = KFTheme.color.keyTextColor,
            maxLines = 1
        )
    }
}
