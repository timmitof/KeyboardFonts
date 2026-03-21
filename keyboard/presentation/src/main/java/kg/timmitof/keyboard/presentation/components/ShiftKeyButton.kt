package kg.timmitof.keyboard.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kg.timmitof.keyboard.presentation.theme.KFTheme

@Composable
internal fun RowScope.ShiftKeyButton(
    weight: Float,
    isActive: Boolean,
    isCapsLock: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (isActive)
        KFTheme.color.keyButtonPressedBackground
    else
        KFTheme.color.keySpecialButtonBackground

    val iconTint = if (isActive)
        KFTheme.color.keyTextColor
    else
        KFTheme.color.keySpecialTextColor

    KeyBase(
        modifier = modifier.weight(weight).fillMaxHeight(),
        background = bg,
        shadowColor = KFTheme.color.keyButtonShadow,
        onClick = onClick
    ) {
        Icon(
            imageVector = if (isCapsLock)
                Icons.Rounded.KeyboardArrowDown
            else
                Icons.Rounded.KeyboardArrowUp,
            contentDescription = "Shift",
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
    }
}