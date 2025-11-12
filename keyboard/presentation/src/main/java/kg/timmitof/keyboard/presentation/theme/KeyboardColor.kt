package kg.timmitof.keyboard.presentation.theme

import androidx.compose.ui.graphics.Color

data class KeyboardLightColor(
    override val backgroundKeyboard: Color = Color(0xFFFFFFFF)
) : KeyboardColorScheme

data class KeyboardDarkColor(
    override val backgroundKeyboard: Color = Color(0xFF000000)
) : KeyboardColorScheme