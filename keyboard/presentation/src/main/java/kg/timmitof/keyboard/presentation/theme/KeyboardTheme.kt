package kg.timmitof.keyboard.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private val keyboardDefaultLightColorScheme = KeyboardLightColor()
private val keyboardDefaultDarkColorScheme = KeyboardDarkColor()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun KeyboardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> keyboardDefaultDarkColorScheme
        else -> keyboardDefaultLightColorScheme
    }

    CompositionLocalProvider(
        LocalKeyboardColorScheme provides colorScheme
    ) {
        MaterialTheme(
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    }
}

object KFTheme {

    val color: KeyboardColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalKeyboardColorScheme.current
}