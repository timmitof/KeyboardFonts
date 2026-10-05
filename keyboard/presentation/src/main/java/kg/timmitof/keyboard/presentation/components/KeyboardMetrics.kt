package kg.timmitof.keyboard.presentation.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kg.timmitof.keyboard.domain.model.KeyboardSettings

internal enum class KeyboardFormFactor(val isLandscape: Boolean, val isTablet: Boolean) {
    PHONE_PORTRAIT(isLandscape = false, isTablet = false),
    PHONE_LANDSCAPE(isLandscape = true, isTablet = false),
    TABLET_PORTRAIT(isLandscape = false, isTablet = true),
    TABLET_LANDSCAPE(isLandscape = true, isTablet = true);

    /** Делить имеет смысл, только когда середина вне досягаемости больших пальцев. */
    val canSplit: Boolean get() = isLandscape || isTablet
}

/** Размеры клавиатуры под устройство и ориентацию; [labelScale] уменьшает подписи вместе с рядами. */
@Immutable
internal data class KeyboardMetrics(
    val formFactor: KeyboardFormFactor,
    val rowHeight: Dp,
    val topBarHeight: Dp,
    val labelScale: Float,
    val isSplit: Boolean,
) {
    val hasHideKey: Boolean get() = formFactor.isTablet

    /** Ширина половины при разделении; `null` — места на две половины нет. */
    fun splitHalfWidth(available: Dp): Dp? {
        if (!isSplit) return null
        val half = when (formFactor) {
            KeyboardFormFactor.PHONE_LANDSCAPE -> min(available * 0.39f, 340.dp)
            KeyboardFormFactor.TABLET_LANDSCAPE -> min(available * 0.37f, 420.dp)
            KeyboardFormFactor.TABLET_PORTRAIT -> min((available - SplitMinGap) / 2, 360.dp)
            KeyboardFormFactor.PHONE_PORTRAIT -> return null
        }
        return half.takeIf { it * 2 + SplitMinGap <= available && it >= MinHalfWidth }
    }

    companion object {
        val Default = KeyboardMetrics(
            formFactor = KeyboardFormFactor.PHONE_PORTRAIT,
            rowHeight = KeyRowHeight,
            topBarHeight = TopBarHeight,
            labelScale = 1f,
            isSplit = false,
        )

        private val SplitMinGap = 44.dp
        private val MinHalfWidth = 220.dp
    }
}

internal val LocalKeyboardMetrics = staticCompositionLocalOf { KeyboardMetrics.Default }

@Composable
internal fun rememberKeyboardMetrics(settings: KeyboardSettings): KeyboardMetrics {
    val configuration = LocalConfiguration.current
    val formFactor = configuration.formFactor()
    val scale = settings.height.scale
    val isSplitEnabled = settings.isSplitEnabled

    return remember(formFactor, scale, isSplitEnabled) {
        val (row, topBar, label) = when (formFactor) {
            KeyboardFormFactor.PHONE_PORTRAIT -> Triple(KeyRowHeight, TopBarHeight, 1f)
            KeyboardFormFactor.PHONE_LANDSCAPE -> Triple(40.dp, 34.dp, 0.82f)
            KeyboardFormFactor.TABLET_PORTRAIT -> Triple(58.dp, 46.dp, 1f)
            KeyboardFormFactor.TABLET_LANDSCAPE -> Triple(54.dp, 46.dp, 1f)
        }
        KeyboardMetrics(
            formFactor = formFactor,
            rowHeight = row * scale,
            topBarHeight = topBar,
            labelScale = label,
            isSplit = isSplitEnabled && formFactor.canSplit,
        )
    }
}

private fun Configuration.formFactor(): KeyboardFormFactor {
    val isTablet = smallestScreenWidthDp >= TabletSmallestWidthDp
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
    return when {
        isTablet && isLandscape -> KeyboardFormFactor.TABLET_LANDSCAPE
        isTablet -> KeyboardFormFactor.TABLET_PORTRAIT
        isLandscape -> KeyboardFormFactor.PHONE_LANDSCAPE
        else -> KeyboardFormFactor.PHONE_PORTRAIT
    }
}

private const val TabletSmallestWidthDp = 600
