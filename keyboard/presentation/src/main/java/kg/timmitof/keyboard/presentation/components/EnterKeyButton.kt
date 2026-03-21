package kg.timmitof.keyboard.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kg.timmitof.keyboard.presentation.theme.KFTheme

@Composable
internal fun RowScope.EnterKeyButton(
    weight: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    KeyBase(
        modifier = modifier.weight(weight).fillMaxHeight(),
        background = KFTheme.color.keySpecialButtonBackground,
        shadowColor = KFTheme.color.keyButtonShadow,
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.Send,
            contentDescription = "Enter",
            tint = KFTheme.color.keySpecialTextColor,
            modifier = Modifier.size(20.dp)
        )
    }
}