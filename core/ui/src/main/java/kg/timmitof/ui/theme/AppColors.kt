package kg.timmitof.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalAppColors = staticCompositionLocalOf<AppColorScheme> { LightColorScheme() }

data class LightColorScheme(
    override val background: Color = Color.White
) : AppColorScheme

data class DarkColorScheme(
    override val background: Color = Color.Black
) : AppColorScheme