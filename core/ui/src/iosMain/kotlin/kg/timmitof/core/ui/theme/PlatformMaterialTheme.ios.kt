package kg.timmitof.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

@Composable
internal actual fun PlatformMaterialTheme(
    colorScheme: ColorScheme,
    typography: Typography,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        typography = typography,
        colorScheme = colorScheme,
        content = content
    )
}
