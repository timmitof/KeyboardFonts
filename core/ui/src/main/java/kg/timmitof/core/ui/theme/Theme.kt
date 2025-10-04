package kg.timmitof.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private val defaultLightColorScheme = LightColorScheme()
private val defaultDarkColorScheme = DarkColorScheme()

private val defaultAppTypography = AppTypography()

@Composable
fun KeyboardFontsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> defaultDarkColorScheme
        else -> defaultLightColorScheme
    }
    val typography = defaultAppTypography

    CompositionLocalProvider(
        LocalAppColors provides colorScheme,
        LocalAppTypography provides typography,
    ) {
        content()
    }
}

object KF {
    val colors: AppColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val typography: AppTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTypography.current
}