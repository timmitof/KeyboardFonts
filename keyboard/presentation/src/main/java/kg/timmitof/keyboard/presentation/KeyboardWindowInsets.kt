package kg.timmitof.keyboard.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max

/**
 * Инсеты системной навигации, измеренные самим окном клавиатуры.
 *
 * Внутри окна IME `WindowInsets.navigationBars` из Compose на части прошивок приходит нулевым
 * (инсеты успевает поглотить decorView сервиса), из-за чего нижний ряд клавиш оказывается
 * под панелью навигации. Поэтому [KeyboardFontsView] измеряет их сам и кладёт сюда.
 */
@Immutable
data class KeyboardWindowInsets(
    val left: Dp = 0.dp,
    val right: Dp = 0.dp,
    val bottom: Dp = 0.dp
)

/** Инсеты окна клавиатуры; по умолчанию нулевые — например, в превью. */
val LocalKeyboardWindowInsets = compositionLocalOf { KeyboardWindowInsets() }

/**
 * Отступ под системную навигацию для контента клавиатуры.
 *
 * Берёт максимум из инсетов Compose и измеренных окном: какой-то из источников на конкретном
 * устройстве может быть нулевым, но оба одновременно — нет. Двойного отступа не возникает,
 * потому что это два измерения одной и той же панели.
 */
@Composable
internal fun keyboardNavigationBarsPadding(): PaddingValues {
    val windowInsets = LocalKeyboardWindowInsets.current
    val layoutDirection = LocalLayoutDirection.current
    val composeInsets = WindowInsets.navigationBars.asPaddingValues()

    return remember(windowInsets, composeInsets, layoutDirection) {
        PaddingValues(
            start = max(windowInsets.left, composeInsets.calculateLeftPadding(layoutDirection)),
            end = max(windowInsets.right, composeInsets.calculateRightPadding(layoutDirection)),
            bottom = max(windowInsets.bottom, composeInsets.calculateBottomPadding())
        )
    }
}
