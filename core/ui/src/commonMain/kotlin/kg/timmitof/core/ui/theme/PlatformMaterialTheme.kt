package kg.timmitof.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

/** MaterialTheme с анимациями платформы: на Android — expressive из material3 1.5, её ещё нет в Compose Multiplatform. */
@Composable
internal expect fun PlatformMaterialTheme(
    colorScheme: ColorScheme,
    typography: Typography,
    content: @Composable () -> Unit,
)
