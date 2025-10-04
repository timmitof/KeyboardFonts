package kg.timmitof.core.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontWeight

data class AppTypography(
    val regular: FontWeight = FontWeight.Normal,
    val medium: FontWeight = FontWeight.Medium,
    val bold: FontWeight = FontWeight.Bold,
)

val LocalAppTypography = staticCompositionLocalOf { AppTypography() }
