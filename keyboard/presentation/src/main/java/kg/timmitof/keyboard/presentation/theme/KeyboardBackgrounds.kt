package kg.timmitof.keyboard.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kg.timmitof.keyboard.domain.model.BackgroundPattern
import kg.timmitof.keyboard.domain.model.KeyboardBackground
import java.io.File
import kotlin.math.hypot

/** Рисуется поверх цвета темы: у [KeyboardBackground.None] своего слоя нет. Фото не задаёт размер — только обрезается под клавиатуру. */
@Composable
fun Modifier.keyboardBackground(background: KeyboardBackground): Modifier = when (background) {
    KeyboardBackground.None -> this
    is KeyboardBackground.Solid -> background(Color(background.argb.toInt()))
    is KeyboardBackground.Pattern -> clipToBounds().drawBehind { drawBackgroundPattern(background.pattern) }
    is KeyboardBackground.Photo -> paint(
        painter = rememberAsyncImagePainter(File(background.path)),
        sizeToIntrinsics = false,
        contentScale = ContentScale.Crop,
    )
}

fun DrawScope.drawBackgroundPattern(pattern: BackgroundPattern) {
    when (pattern) {
        BackgroundPattern.MINT -> drawDots(base = Color(0xFFCDEFE7), dot = Color(0xFF7FD8C4), step = 12f, radius = 1.6f)
        BackgroundPattern.STRIPES -> drawStripes()
        BackgroundPattern.BUBBLES -> drawBubbles()
        BackgroundPattern.GRID -> drawGrid()
        BackgroundPattern.WAVES -> drawWaves()
        BackgroundPattern.NIGHT -> {
            drawDots(base = Color(0xFF1E1A1F), dot = Color(0xFFE3B7F3), step = 14f, radius = 1f)
            drawDots(base = null, dot = Color(0xFF7FD8C4), step = 14f, radius = 1f, shift = 7f)
        }
    }
}

private fun DrawScope.drawDots(base: Color?, dot: Color, step: Float, radius: Float, shift: Float = 0f) {
    base?.let(::drawRect)
    val stepPx = step.dp.toPx()
    val radiusPx = radius.dp.toPx()
    var y = stepPx / 2 + shift.dp.toPx()
    while (y < size.height + stepPx) {
        var x = stepPx / 2 + shift.dp.toPx()
        while (x < size.width + stepPx) {
            drawCircle(dot, radiusPx, Offset(x, y))
            x += stepPx
        }
        y += stepPx
    }
}

private fun DrawScope.drawStripes() {
    drawRect(Color(0xFFFFE3B8))
    val period = 14.dp.toPx()
    val stripe = 6.dp.toPx()
    val stripeColor = Color(0xFFFFD08A)

    // Полоса под 45°: параллелограмм, сдвинутый на высоту по горизонтали.
    var x = -size.height
    while (x < size.width) {
        val path = Path().apply {
            moveTo(x + period - stripe, 0f)
            lineTo(x + period, 0f)
            lineTo(x + period + size.height, size.height)
            lineTo(x + period - stripe + size.height, size.height)
            close()
        }
        drawPath(path, stripeColor)
        x += period
    }
}

private fun DrawScope.drawBubbles() {
    drawRect(Color(0xFFF7D8FF))
    val tile = 96.dp.toPx()

    var top = 0f
    while (top < size.height) {
        var left = 0f
        while (left < size.width) {
            drawCircle(Color(0xFFE3B7F3), tile * 0.16f, Offset(left + tile * 0.22f, top + tile * 0.30f))
            drawCircle(Color(0xFFD9C6F5), tile * 0.22f, Offset(left + tile * 0.78f, top + tile * 0.72f))
            drawCircle(Color(0xFFFFDAD8), tile * 0.09f, Offset(left + tile * 0.60f, top + tile * 0.18f))
            left += tile
        }
        top += tile
    }
}

private fun DrawScope.drawGrid() {
    drawRect(Color(0xFFFFF7FB))
    val step = 12.dp.toPx()
    val line = 1.dp.toPx()
    val color = Color(0xFFE9E0E7)

    var x = 0f
    while (x < size.width) {
        drawRect(color, Offset(x, 0f), Size(line, size.height))
        x += step
    }
    var y = 0f
    while (y < size.height) {
        drawRect(color, Offset(0f, y), Size(size.width, line))
        y += step
    }
}

/** Кольца расходятся из точки под нижним краем — как на макете. */
private fun DrawScope.drawWaves() {
    val center = Offset(size.width / 2, size.height * 1.2f)
    val period = 16.dp.toPx()
    val inner = 10.dp.toPx()
    val maxRadius = hypot(size.width / 2, center.y)

    clipRect {
        drawRect(Color(0xFFF5B7B5))
        var radius = (maxRadius / period).toInt() * period + period
        while (radius > 0f) {
            drawCircle(Color(0xFFF5B7B5), radius, center)
            drawCircle(Color(0xFFFFDAD8), radius - (period - inner), center)
            radius -= period
        }
    }
}
