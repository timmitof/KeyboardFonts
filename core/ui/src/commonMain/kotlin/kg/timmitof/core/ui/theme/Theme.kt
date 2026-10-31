package kg.timmitof.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

@Composable
fun KeyboardFontsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> AppDarkColorScheme
        else -> AppLightColorScheme
    }

    val fontFamily = poppinsFontFamily()
    val typography = remember(fontFamily) { appTypography(fontFamily) }

    CompositionLocalProvider(
        LocalAppColors provides if (darkTheme) AppDarkColors else AppLightColors
    ) {
        PlatformMaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
