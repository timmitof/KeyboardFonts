package kg.timmitof.keyboard.presentation.components.overlay.clipboard

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardEntry
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.presentation.screens.keyboard.states.ClipboardAction
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.theme.KFTheme

@Composable
internal fun ClipboardCardMenu(
    entry: ClipboardEntry,
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onEvent: (KeyboardEvent) -> Unit,
) {
    if (!isOpen) return

    Popup(
        alignment = Alignment.TopCenter,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = false),
    ) {
        Column(
            modifier = Modifier
                .width(MenuWidth)
                .shadow(elevation = 8.dp, shape = MenuShape)
                .clip(MenuShape)
                .background(KFTheme.color.keyButtonBackground)
                .padding(5.dp),
        ) {
            MenuItem(
                iconRes = R.drawable.ic_clipboard_pin,
                label = stringResource(
                    if (entry.isPinned) R.string.clipboard_unpin else R.string.clipboard_pin
                ),
            ) {
                onEvent(KeyboardEvent.OnClipboardAction(ClipboardAction.Pin(entry, !entry.isPinned)))
                onDismiss()
            }

            MenuItem(
                iconRes = R.drawable.ic_trash,
                label = stringResource(R.string.clipboard_delete),
            ) {
                onEvent(KeyboardEvent.OnClipboardAction(ClipboardAction.Remove(entry)))
                onDismiss()
            }
        }
    }
}

@Composable
private fun MenuItem(
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(iconRes),
            contentDescription = null,
            tint = KFTheme.color.overlayIconColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            fontSize = 13.sp,
            color = KFTheme.color.keyTextColor,
            maxLines = 1,
        )
    }
}

private val MenuWidth = 184.dp
private val MenuShape = RoundedCornerShape(12.dp)
