package kg.timmitof.core.ui.theme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun KeyboardFontsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val dynamicColor = dynamicTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
//        dynamicColor -> getDynamicColorScheme(darkTheme)
        darkTheme -> AppDarkColorScheme
        else -> AppLightColorScheme
    }

    MaterialTheme(
        typography = AppTypography,
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun getDynamicColorScheme(isDarkTheme: Boolean): ColorScheme = when {
    isDarkTheme -> dynamicDarkColorScheme(LocalContext.current)
    else -> dynamicLightColorScheme(LocalContext.current)
}