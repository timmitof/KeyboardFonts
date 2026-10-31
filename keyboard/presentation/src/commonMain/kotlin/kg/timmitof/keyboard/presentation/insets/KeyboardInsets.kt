package kg.timmitof.keyboard.presentation.insets

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class KeyboardInsets(
    val left: Dp = 0.dp,
    val right: Dp = 0.dp,
    val bottom: Dp = 0.dp
)

val LocalKeyboardInsets = compositionLocalOf { KeyboardInsets() }
