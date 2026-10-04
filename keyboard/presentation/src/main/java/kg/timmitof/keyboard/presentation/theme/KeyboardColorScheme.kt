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
    val keyEnterBackground: Color
    val keyEnterTextColor: Color
    val keyEnterPressedBackground: Color
    val keyOutline: Color
    val noticeBackground: Color
    val noticeTextColor: Color

    val overlayBackground: Color
    val overlayTitleColor: Color
    val overlaySubtitleColor: Color
    val overlayIconColor: Color
    val overlayAccent: Color
    val overlayAccentTextColor: Color
    val overlayDivider: Color

    val overlayChipBackground: Color
    val overlayChipTextColor: Color

    val overlayActionBackground: Color
    val overlayActionTextColor: Color

    val overlayControlTrack: Color
    val overlayControlLabelColor: Color
}
