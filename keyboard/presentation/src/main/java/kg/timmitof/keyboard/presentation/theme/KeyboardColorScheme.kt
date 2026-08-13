package kg.timmitof.keyboard.presentation.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalKeyboardColorScheme = compositionLocalOf<KeyboardColorScheme> { KeyboardLightColor() }

interface KeyboardColorScheme {
    val keyboardBackground: Color
    val keyButtonBackground: Color
    val keyTextColor: Color
    val keyButtonPressedBackground: Color
    val keyButtonShadow: Color
    val keySpecialButtonBackground: Color
    val keySpecialTextColor: Color
    val keyAccentBackground: Color
    val keyAccentTextColor: Color
    val noticeBackground: Color
    val noticeTextColor: Color

    /**
     * Окно быстрых настроек и буфера.
     */
    val overlayBackground: Color
    val overlayTitleColor: Color
    val overlaySubtitleColor: Color
    val overlayIconColor: Color
    val overlayAccent: Color
    val overlayAccentTextColor: Color
    val overlayDivider: Color

    /** Ярлыки-«таблетки» под разделителем: язык, вибрация, тема. */
    val overlayChipBackground: Color
    val overlayChipTextColor: Color

    /** Строка перехода в приложение — по виду широкая клавиша. */
    val overlayActionBackground: Color
    val overlayActionTextColor: Color

    /** Дорожка переключателя и сегментов в покое. */
    val overlayControlTrack: Color
    val overlayControlLabelColor: Color
}
