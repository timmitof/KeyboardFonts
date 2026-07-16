package kg.timmitof.keyboard.presentation.components.keys

import kg.timmitof.keyboard.presentation.R
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import kg.timmitof.core.ui.holdSlideClickable
import kg.timmitof.keyboard.presentation.theme.KFTheme

@Composable
internal fun RowScope.BackspaceKeyButton(
    weight: Float,
    modifier: Modifier = Modifier,
    onDeleteWord: () -> Unit = {},
    onSelectChange: (chars: Int) -> Unit = {},
    onSelectCommit: (chars: Int) -> Unit = {},
    onClick: () -> Unit
) {
    KeyBase(
        modifier = modifier.weight(weight).fillMaxHeight(),
        background = KFTheme.color.keySpecialButtonBackground,
        shadowColor = KFTheme.color.keyButtonShadow,
        customGestures = { interactionSource ->
            Modifier.holdSlideClickable(
                interactionSource = interactionSource,
                onTap = onClick,
                onHold = onDeleteWord,
                onSlideChange = onSelectChange,
                onSlideFinish = onSelectCommit
            )
        },
        onClick = onClick
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_backspace_key),
            contentDescription = "Backspace",
            tint = KFTheme.color.keySpecialTextColor,
        )
    }
}