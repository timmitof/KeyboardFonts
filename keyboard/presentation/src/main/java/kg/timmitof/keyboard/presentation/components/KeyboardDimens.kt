package kg.timmitof.keyboard.presentation.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

internal val KeyRowHeight = 60.dp

internal val LocalKeyRowHeight = staticCompositionLocalOf { KeyRowHeight }

internal val KeyRowSpacing = 8.dp

internal val KeySpacing = 6.dp

internal val KeyCornerRadius = 11.dp

internal val KeyShape = RoundedCornerShape(KeyCornerRadius)

internal val KeySupport = 1.5.dp

internal val TopBarHeight = 40.dp

internal const val KeyRowCount = 4
