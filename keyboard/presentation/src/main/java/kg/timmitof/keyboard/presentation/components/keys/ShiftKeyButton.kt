package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import kg.timmitof.keyboard.presentation.screens.keyboard.states.ShiftState
import kg.timmitof.keyboard.presentation.screens.keyboard.states.isUpperCase
import kg.timmitof.keyboard.presentation.theme.KFTheme

@Composable
internal fun RowScope.ShiftKeyButton(
    modifier: Modifier = Modifier,
    weight: Float,
    shiftState: ShiftState = ShiftState.DISABLED,
    onClick: () -> Unit
) {
    val background = animateColorAsState(
        targetValue =
            if (shiftState.isUpperCase()) KFTheme.color.keyButtonPressedBackground
            else KFTheme.color.keySpecialButtonBackground
    )

    val iconTint = animateColorAsState(
        targetValue =
            if (shiftState.isUpperCase()) KFTheme.color.keyTextColor
            else KFTheme.color.keySpecialTextColor
    )

    KeyBase(
        modifier = modifier.weight(weight).fillMaxHeight(),
        background = background.value,
        shadowColor = KFTheme.color.keyButtonShadow,
        onClick = onClick
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(shiftState.icon),
            contentDescription = "Shift",
            tint = iconTint.value,
        )
    }
}