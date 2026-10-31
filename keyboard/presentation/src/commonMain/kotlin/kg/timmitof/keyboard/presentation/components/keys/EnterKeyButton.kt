package kg.timmitof.keyboard.presentation.components.keys

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
import androidx.compose.ui.unit.dp
import kg.timmitof.keyboard.presentation.resources.Res
import kg.timmitof.keyboard.presentation.resources.ic_back_key
import kg.timmitof.keyboard.presentation.resources.ic_check_key
import kg.timmitof.keyboard.presentation.resources.ic_enter_key
import kg.timmitof.keyboard.presentation.resources.ic_go_key
import kg.timmitof.keyboard.presentation.resources.ic_next_key
import kg.timmitof.keyboard.presentation.resources.ic_search_key
import kg.timmitof.keyboard.presentation.resources.ic_send_key
import kg.timmitof.keyboard.presentation.screens.keyboard.states.EnterAction
import kg.timmitof.keyboard.presentation.sound.KeySound
import kg.timmitof.keyboard.presentation.theme.KFTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource

@Composable
internal fun RowScope.EnterKeyButton(
    weight: Float,
    modifier: Modifier = Modifier,
    enterAction: EnterAction = EnterAction.RETURN,
    onClick: () -> Unit
) {
    KeyBase(
        modifier = modifier.weight(weight).fillMaxHeight(),
        background = KFTheme.color.keyEnterBackground,
        shadowColor = KFTheme.color.keyButtonShadow,
        pressedBackground = KFTheme.color.keyEnterPressedBackground,
        sound = KeySound.ENTER,
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
                imageVector = vectorResource(action.iconRes),
                contentDescription = action.name,
                tint = KFTheme.color.keyEnterTextColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private val EnterAction.iconRes: DrawableResource
    get() = when (this) {
        EnterAction.RETURN -> Res.drawable.ic_enter_key
        EnterAction.GO -> Res.drawable.ic_go_key
        EnterAction.SEARCH -> Res.drawable.ic_search_key
        EnterAction.SEND -> Res.drawable.ic_send_key
        EnterAction.NEXT -> Res.drawable.ic_next_key
        EnterAction.PREVIOUS -> Res.drawable.ic_back_key
        EnterAction.DONE -> Res.drawable.ic_check_key
    }
