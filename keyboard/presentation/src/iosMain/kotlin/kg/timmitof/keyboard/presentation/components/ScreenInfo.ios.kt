package kg.timmitof.keyboard.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalWindowInfo
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIScreen

/** Точки iOS — те же dp. Размер окна только переключает пересчёт: поворот меняет и его. */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun currentScreenInfo(): ScreenInfo {
    LocalWindowInfo.current.containerSize
    val (width, height) = UIScreen.mainScreen.bounds.useContents { size.width.toInt() to size.height.toInt() }
    return ScreenInfo(
        heightDp = height,
        smallestWidthDp = minOf(width, height),
        isLandscape = width > height,
    )
}
