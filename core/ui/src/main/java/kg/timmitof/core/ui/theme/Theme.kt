package kg.timmitof.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun KeyboardFontsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> AppDarkColorScheme
        else -> AppLightColorScheme
    }

    CompositionLocalProvider(
        LocalAppColors provides if (darkTheme) AppDarkColors else AppLightColors
    ) {
        MaterialTheme(
            typography = AppTypography,
            colorScheme = colorScheme,
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    }
}
