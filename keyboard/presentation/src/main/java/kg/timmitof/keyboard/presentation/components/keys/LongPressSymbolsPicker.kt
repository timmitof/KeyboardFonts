package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kg.timmitof.keyboard.presentation.components.AboveAnchorPopupPositionProvider
import kg.timmitof.keyboard.presentation.theme.KFTheme

/** Размер ячейки символа в попапе. */
internal val LongPressSymbolCellSize = 38.dp

/**
 * Попап выбора символа по зажатию клавиши
 *
 * @param symbols варианты символов.
 * @param selectedIndex индекс символа под пальцем.
 * @param onDismiss запрос на закрытие попапа.
 */
@Composable
internal fun LongPressSymbolsPicker(
    symbols: List<String>,
    selectedIndex: () -> Int,
    onDismiss: () -> Unit,
) {
    val marginPx = with(LocalDensity.current) { 4.dp.roundToPx() }
    val positionProvider = remember(marginPx) { AboveAnchorPopupPositionProvider(marginPx) }
    val shape = RoundedCornerShape(14.dp)
    val highlightColor = KFTheme.color.keySpecialButtonBackground

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = false, clippingEnabled = false)
    ) {
        Row(
            modifier = Modifier
                .shadow(elevation = 6.dp, shape = shape)
                .background(color = KFTheme.color.keyButtonBackground, shape = shape)
                .padding(4.dp)
        ) {
            symbols.forEachIndexed { index, symbol ->
                Box(
                    modifier = Modifier
                        .size(LongPressSymbolCellSize)
                        .drawBehind {
                            if (index == selectedIndex()) {
                                drawRoundRect(
                                    color = highlightColor,
                                    cornerRadius = CornerRadius(10.dp.toPx())
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = symbol,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = KFTheme.color.keyTextColor
                    )
                }
            }
        }
    }
}
