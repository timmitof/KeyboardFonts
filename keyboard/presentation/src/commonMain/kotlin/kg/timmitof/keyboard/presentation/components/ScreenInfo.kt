package kg.timmitof.keyboard.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

/** Экран устройства, а не окна клавиатуры: окно IME низкое, по нему не понять ни планшет, ни ориентацию. */
@Immutable
internal data class ScreenInfo(
    val heightDp: Int,
    val smallestWidthDp: Int,
    val isLandscape: Boolean,
)

@Composable
internal expect fun currentScreenInfo(): ScreenInfo
