package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
internal fun KeyBase(
    modifier: Modifier = Modifier,
    background: Color,
    shadowColor: Color,
    customGestures: ((MutableInteractionSource) -> Modifier)? = null,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(80), label = "scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 3.dp,
        animationSpec = tween(80), label = "elevation"
    )

    val clickModifier = when {
        customGestures != null -> customGestures(interactionSource)

        else -> Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    }

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(elevation, RoundedCornerShape(10.dp), spotColor = shadowColor)
            .background(background, RoundedCornerShape(10.dp))
            .then(clickModifier),
        contentAlignment = Alignment.Center,
        content = content
    )
}