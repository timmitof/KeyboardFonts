package kg.timmitof.core.ui.theme

import androidx.compose.ui.graphics.Color

/** Тон 0–360, насыщенность и яркость 0–1 — как `android.graphics.Color.colorToHSV`. */
fun Color.toHsv(): FloatArray {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val delta = max - min

    val hue = when {
        delta == 0f -> 0f
        max == red -> 60f * (((green - blue) / delta) % 6f)
        max == green -> 60f * ((blue - red) / delta + 2f)
        else -> 60f * ((red - green) / delta + 4f)
    }.let { if (it < 0f) it + 360f else it }

    return floatArrayOf(hue, if (max == 0f) 0f else delta / max, max)
}
