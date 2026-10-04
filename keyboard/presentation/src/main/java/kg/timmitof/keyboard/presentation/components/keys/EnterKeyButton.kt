package kg.timmitof.keyboard.presentation.components.keys

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.presentation.screens.keyboard.states.EnterAction
import kg.timmitof.keyboard.presentation.theme.KFTheme

@Composable
internal fun RowScope.EnterKeyButton(
    weight: Float,
    modifier: Modifier = Modifier,
    enterAction: EnterAction = EnterAction.RETURN,
    onClick: () -> Unit
) {
    KeyBase(
        modifier = modifier.weight(weight).fillMaxHeight(),
        background = KFTheme.color.keySpecialButtonBackground,
        shadowColor = KFTheme.color.keyButtonShadow,
        onClick = onClick
    ) {
        AnimatedContent(
            targetState = enterAction,
            transitionSpec = {
                (fadeIn() + scaleIn(initialScale = 0.6f)) togetherWith
                    (fadeOut() + scaleOut(targetScale = 0.6f))
            },
            label = "enterIcon"
        ) { action ->
            Icon(
                imageVector = ImageVector.vectorResource(action.iconRes),
                contentDescription = action.name,
                tint = KFTheme.color.keySpecialTextColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private val EnterAction.iconRes: Int
    @DrawableRes get() = when (this) {
        EnterAction.RETURN -> R.drawable.ic_enter_key
        EnterAction.GO -> R.drawable.ic_go_key
        EnterAction.SEARCH -> R.drawable.ic_search_key
        EnterAction.SEND -> R.drawable.ic_send_key
        EnterAction.NEXT -> R.drawable.ic_next_key
        EnterAction.PREVIOUS -> R.drawable.ic_back_key
        EnterAction.DONE -> R.drawable.ic_check_key
    }
