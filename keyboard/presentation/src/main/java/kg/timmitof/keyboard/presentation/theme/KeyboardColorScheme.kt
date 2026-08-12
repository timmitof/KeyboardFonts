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
}
