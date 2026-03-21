package kg.timmitof.keyboard.presentation.theme

import androidx.compose.ui.graphics.Color

data class KeyboardLightColor(
    override val keyboardBackground: Color = Color(0xFFE8E8E8),
    override val keyButtonBackground: Color = Color(0xFFFFFFFF),
    override val keyTextColor: Color = Color(0xFF000000),
    override val keyButtonPressedBackground: Color = Color(0xFFD0D0D0),
    override val keyButtonShadow: Color = Color(0xFF9E9E9E),
    override val keySpecialButtonBackground: Color = Color(0xFFCECECE),
    override val keySpecialTextColor: Color = Color(0xFF3A3A3A),
) : KeyboardColorScheme

data class KeyboardDarkColor(
    override val keyboardBackground: Color = Color(0xFF1A1A1A),
    override val keyButtonBackground: Color = Color(0xFF2A2A2A),
    override val keyTextColor: Color = Color(0xFFFFFFFF),
    override val keyButtonPressedBackground: Color = Color(0xFF3D3D3D),
    override val keyButtonShadow: Color = Color(0xFF000000),
    override val keySpecialButtonBackground: Color = Color(0xFF222222),
    override val keySpecialTextColor: Color = Color(0xFFAAAAAA),
) : KeyboardColorScheme