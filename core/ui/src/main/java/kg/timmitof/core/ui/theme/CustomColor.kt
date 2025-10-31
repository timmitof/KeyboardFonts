package kg.timmitof.core.ui.theme

import androidx.compose.ui.graphics.Color

interface CustomColor {
    val cardBackground: Color
}

class AppColorLight(
    override val cardBackground: Color = Color(0xFFFFFFFF)
): CustomColor

class AppColorDark(
    override val cardBackground: Color = Color(0xFF343A4B)
): CustomColor