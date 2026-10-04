package kg.timmitof.core.ui.components.color

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ColorPickerDialog(
    initial: Color,
    title: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: (Color) -> Unit,
    onDismiss: () -> Unit,
) {
    val start = remember(initial) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(initial.toArgb(), it) }
    }
    var hue by remember(initial) { mutableFloatStateOf(start[0]) }
    var saturation by remember(initial) { mutableFloatStateOf(start[1]) }
    var value by remember(initial) { mutableFloatStateOf(start[2]) }
    val color by remember { derivedStateOf { Color.hsv(hue, saturation, value) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        confirmButton = {
            TextButton(onClick = { onConfirm(color) }) { Text(text = confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = dismissLabel) }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SaturationValuePanel(
                    hue = { hue },
                    saturation = { saturation },
                    value = { value },
                    onChange = { s, v ->
                        saturation = s
                        value = v
                    },
                )
                HueBar(hue = { hue }, onChange = { hue = it })
                ColorPreview(color = { color })
            }
        },
    )
}

@Composable
private fun SaturationValuePanel(
    hue: () -> Float,
    saturation: () -> Float,
    value: () -> Float,
    onChange: (saturation: Float, value: Float) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(PanelHeight)
            .clip(PanelShape)
            .trackDrag { position, size ->
                onChange(
                    (position.x / size.width).coerceIn(0f, 1f),
                    1f - (position.y / size.height).coerceIn(0f, 1f),
                )
            }
            .drawWithContent {
                drawRect(Brush.horizontalGradient(listOf(Color.White, Color.hsv(hue(), 1f, 1f))))
                drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                drawThumb(
                    center = Offset(saturation() * size.width, (1f - value()) * size.height),
                    color = Color.hsv(hue(), saturation(), value()),
                )
            }
    )
}

@Composable
private fun HueBar(hue: () -> Float, onChange: (Float) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BarHeight)
            .trackDrag { position, size ->
                onChange((position.x / size.width).coerceIn(0f, 1f) * MaxHue)
            }
            .drawBehind {
                val radius = size.height / 2
                drawRoundRect(
                    brush = Brush.horizontalGradient(HueColors, startX = radius, endX = size.width - radius),
                    cornerRadius = CornerRadius(radius),
                )
                drawThumb(
                    center = Offset(radius + hue() / MaxHue * (size.width - radius * 2), radius),
                    color = Color.hsv(hue(), 1f, 1f),
                )
            }
    )
}

@Composable
private fun ColorPreview(color: () -> Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .drawBehind { drawRect(color()) }
        )
        Text(
            text = color().toHex(),
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Касание сразу двигает ползунок, дальше — ведём за пальцем. */
private fun Modifier.trackDrag(onMove: (Offset, IntSize) -> Unit): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        val down = awaitFirstDown()
        onMove(down.position, size)
        down.consume()
        drag(down.id) { change ->
            onMove(change.position, size)
            change.consume()
        }
    }
}

private fun DrawScope.drawThumb(center: Offset, color: Color) {
    val radius = ThumbRadius.toPx()
    drawCircle(color = color, radius = radius, center = center)
    drawCircle(color = Color.White, radius = radius, center = center, style = Stroke(ThumbStroke.toPx()))
}

private fun Color.toHex(): String = "#%06X".format(toArgb() and 0xFFFFFF)

private const val MaxHue = 360f

private val PanelHeight = 168.dp
private val BarHeight = 24.dp
private val PanelShape = RoundedCornerShape(14.dp)
private val ThumbRadius = 10.dp
private val ThumbStroke = 2.5.dp
