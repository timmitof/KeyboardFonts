package kg.timmitof.keyboard.presentation.components

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

/** Позиционирует попап по центру над якорем; при выходе за верх окна — под якорем. */
internal class AboveAnchorPopupPositionProvider(
    private val marginPx: Int,
) : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = (anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2)
            .coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))

        val above = anchorBounds.top - popupContentSize.height - marginPx
        val y = if (above >= 0) above else anchorBounds.bottom + marginPx

        return IntOffset(x, y)
    }
}
