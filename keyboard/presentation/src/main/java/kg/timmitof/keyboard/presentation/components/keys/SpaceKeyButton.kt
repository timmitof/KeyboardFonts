package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kg.timmitof.keyboard.presentation.theme.KFTheme

@Composable
internal fun RowScope.SpaceKeyButton(
    weight: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    KeyBase(
        modifier = modifier.weight(weight).fillMaxHeight(),
        background = KFTheme.color.keyButtonBackground,
        shadowColor = KFTheme.color.keyButtonShadow,
        onClick = onClick
    ) {}
}