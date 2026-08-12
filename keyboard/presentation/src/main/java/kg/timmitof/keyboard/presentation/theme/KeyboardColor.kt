package kg.timmitof.keyboard.presentation.theme

import androidx.compose.ui.graphics.Color

data class KeyboardLightColor(
    override val keyboardBackground: Color = Color(0xFFE8E8E8),
    override val keyButtonBackground: Color = Color(0xFFFFFFFF),
    override val keyTextColor: Color = Color(0xFF1B1D21),
    override val keyButtonPressedBackground: Color = Color(0xFFD0D0D0),
    override val keyButtonShadow: Color = Color(0x2E000000),
    override val keySpecialButtonBackground: Color = Color(0xFFBFC4CB),
    override val keySpecialTextColor: Color = Color(0xFF1B1D21),
    override val keyAccentBackground: Color = Color(0xFF6D4BD8),
    override val keyAccentTextColor: Color = Color(0xFFFFFFFF),
    override val noticeBackground: Color = Color(0xFFDCD3EF),
    override val noticeTextColor: Color = Color(0xFF3F2A8C),
) : KeyboardColorScheme

data class KeyboardDarkColor(
    override val keyboardBackground: Color = Color(0xFF1A1A1A),
    override val keyButtonBackground: Color = Color(0xFF303030),
    override val keyTextColor: Color = Color(0xFFFFFFFF),
    override val keyButtonPressedBackground: Color = Color(0xFF3D3D3D),
    override val keyButtonShadow: Color = Color(0x99000000),
    override val keySpecialButtonBackground: Color = Color(0xFF1F1F1F),
    override val keySpecialTextColor: Color = Color(0xFFB9B9B9),
    override val keyAccentBackground: Color = Color(0xFFE3B7F3),
    override val keyAccentTextColor: Color = Color(0xFF3A1B52),
    override val noticeBackground: Color = Color(0xFF2E2440),
    override val noticeTextColor: Color = Color(0xFFD9C6F5),
) : KeyboardColorScheme
