package kg.timmitof.keyboard.presentation.components.emoji

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kg.timmitof.keyboard.presentation.components.AboveAnchorPopupPositionProvider
import kg.timmitof.keyboard.presentation.theme.KFTheme

/**
 * Попап выбора варианта тона кожи эмодзи.
 */
@Composable
internal fun EmojiVariantPicker(
    variants: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val marginPx = with(LocalDensity.current) { 4.dp.roundToPx() }
    val positionProvider = remember(marginPx) { AboveAnchorPopupPositionProvider(marginPx) }
    val shape = RoundedCornerShape(14.dp)

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
            variants.forEach { variant ->
                val isSelected = variant == selected
                EmojiCell(
                    emoji = variant,
                    onClick = { onSelect(variant) },
                    modifier = Modifier
                        .size(38.dp)
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    color = KFTheme.color.keySpecialButtonBackground,
                                    shape = CircleShape
                                )
                            } else {
                                Modifier
                            }
                        )
                )
            }
        }
    }
}
