package kg.timmitof.core.ui.components.color

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kg.timmitof.core.ui.R

/** [onPickCustom] = `null` — без кружка «свой цвет»; цвет не из [colors] показывается в нём. */
@Composable
fun ColorSwatches(
    colors: List<Color>,
    selected: Color?,
    onSelect: (Color) -> Unit,
    modifier: Modifier = Modifier,
    onPickCustom: (() -> Unit)? = null,
) {
    val custom = selected?.takeIf { it !in colors }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SwatchGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        colors.forEach { color ->
            Swatch(
                fill = { drawCircle(color) },
                isSelected = color == selected,
                onClick = { onSelect(color) },
            )
        }

        onPickCustom?.let { onClick ->
            CustomSwatch(color = custom, onClick = onClick)
        }
    }
}

@Composable
private fun CustomSwatch(color: Color?, onClick: () -> Unit) {
    Swatch(
        fill = {
            drawCircle(Brush.sweepGradient(HueColors))
            color?.let { drawCircle(it, radius = size.minDimension / 2 - RainbowRim.toPx()) }
        },
        isSelected = color != null,
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_plus),
            contentDescription = null,
            tint = if (color != null && color.luminance() > 0.5f) Color.Black else Color.White,
            modifier = Modifier.size(11.dp),
        )
    }
}

@Composable
private fun Swatch(
    fill: DrawScope.() -> Unit,
    isSelected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit = {},
) {
    val ring by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 700f),
        label = "swatchRing",
    )
    val ringColor = MaterialTheme.colorScheme.onBackground

    // Кольцо рисуется в draw-фазе вокруг кружка: выбор не меняет размеры и не вызывает рекомпозиций.
    Box(
        modifier = Modifier
            .size(SwatchSize + RingSpace * 2)
            .drawBehind {
                if (ring > 0f) {
                    val width = RingWidth.toPx() * ring
                    drawCircle(
                        color = ringColor,
                        radius = size.minDimension / 2 - width / 2,
                        style = Stroke(width = width),
                    )
                }
            }
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(SwatchSize)
                .drawBehind(fill),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

internal val HueColors = listOf(
    Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red,
)

private val SwatchSize = 22.dp
private val RingSpace = 4.dp
private val RingWidth = 2.dp
private val SwatchGap = 4.dp
private val RainbowRim = 3.dp
