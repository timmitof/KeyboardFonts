package kg.timmitof.keyboard.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import kg.timmitof.keyboard.domain.model.BackgroundPattern
import kg.timmitof.keyboard.domain.model.KeyboardBackground

/** Цвета клавиш, подобранные под фон; [isDark] — какая основа темы читается на этом фоне. */
@Immutable
data class BackgroundPalette(
    val isDark: Boolean,
    val key: Color,
    val special: Color,
    val enter: Color,
)

fun KeyboardBackground.palette(): BackgroundPalette? = when (this) {
    KeyboardBackground.None -> null
    is KeyboardBackground.Solid -> paletteFromTone(Color(argb.toInt()))
    is KeyboardBackground.Photo -> paletteFromTone(Color(photo.tone.toInt()))
    is KeyboardBackground.Pattern -> pattern.palette
}

private val BackgroundPattern.palette: BackgroundPalette
    get() = when (this) {
        BackgroundPattern.MINT -> BackgroundPalette(false, Color(0xFFFFFFFF), Color(0xFFA8E3D6), Color(0xFF0F7B6C))
        BackgroundPattern.STRIPES -> BackgroundPalette(false, Color(0xFFFFF8EE), Color(0xFFFFC97A), Color(0xFF8A5200))
        BackgroundPattern.BUBBLES -> BackgroundPalette(false, Color(0xFFFFFFFF), Color(0xFFE3B7F3), Color(0xFF6D4BD8))
        BackgroundPattern.GRID -> BackgroundPalette(false, Color(0xFFFFFFFF), Color(0xFFE9E0E7), Color(0xFF1E1A1F))
        BackgroundPattern.WAVES -> BackgroundPalette(false, Color(0xFFFFF5F4), Color(0xFFF5B7B5), Color(0xFF815251))
        BackgroundPattern.NIGHT -> BackgroundPalette(true, Color(0xFF2D282E), Color(0xFF3A333B), Color(0xFFE3B7F3))
    }

/** Для цвета и фото палитра выводится из оттенка: клавиши — его светлые или тёмные родственники. */
private fun paletteFromTone(tone: Color): BackgroundPalette {
    val hsv = FloatArray(3).also { android.graphics.Color.colorToHSV(tone.toArgb(), it) }
    val isGray = hsv[1] < GraySaturation

    return if (tone.luminance() < DarkLuminance) {
        BackgroundPalette(
            isDark = true,
            key = lerp(tone, Color.White, 0.16f),
            special = lerp(tone, Color.White, 0.08f),
            enter = if (isGray) Color(0xFFE3B7F3) else Color.hsv(hsv[0], 0.35f, 0.95f),
        )
    } else {
        BackgroundPalette(
            isDark = false,
            key = lerp(tone, Color.White, 0.82f),
            special = lerp(tone, Color.Black, 0.12f),
            enter = if (isGray) Color(0xFF6D4BD8) else Color.hsv(hsv[0], maxOf(hsv[1], 0.55f), 0.5f),
        )
    }
}

private const val DarkLuminance = 0.35f
private const val GraySaturation = 0.12f
