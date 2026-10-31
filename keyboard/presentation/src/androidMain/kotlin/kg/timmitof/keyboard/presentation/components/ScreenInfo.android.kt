package kg.timmitof.keyboard.presentation.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
internal actual fun currentScreenInfo(): ScreenInfo {
    val configuration = LocalConfiguration.current
    return ScreenInfo(
        heightDp = configuration.screenHeightDp,
        smallestWidthDp = configuration.smallestScreenWidthDp,
        isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE,
    )
}
