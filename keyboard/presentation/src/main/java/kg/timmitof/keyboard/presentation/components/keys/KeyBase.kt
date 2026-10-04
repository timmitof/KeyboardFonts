package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kg.timmitof.core.ui.keyClickable
import kg.timmitof.keyboard.presentation.components.KeyCornerRadius
import kg.timmitof.keyboard.presentation.components.KeyRowSpacing
import kg.timmitof.keyboard.presentation.components.KeySpacing
import kg.timmitof.keyboard.presentation.components.KeySupport
import kg.timmitof.keyboard.presentation.sound.KeySound
import kg.timmitof.keyboard.presentation.theme.KFTheme

@Composable
internal fun KeyBase(
    modifier: Modifier = Modifier,
    background: Color,
    shadowColor: Color,
    pressedBackground: Color = KFTheme.color.keyButtonPressedBackground,
    sound: KeySound = KeySound.STANDARD,
    interactionSource: MutableInteractionSource? = null,
    customGestures: ((MutableInteractionSource) -> Modifier)? = null,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val source = remember(interactionSource) { interactionSource ?: MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()

    // Вибрация и звук — на касание, а не на ввод: отклик должен опережать символ.
    val feedback = LocalKeyFeedback.current
    LaunchedEffect(isPressed) {
        if (isPressed) feedback.onKeyPress(sound)
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(80), label = "scale"
    )
    val surface by animateColorAsState(
        targetValue = if (isPressed) pressedBackground else background,
        animationSpec = tween(80), label = "surface"
    )

    val outline = KFTheme.color.keyOutline.takeIf { KFTheme.isKeyOutlined }

    val clickModifier = customGestures?.invoke(source)
        ?: Modifier.keyClickable(interactionSource = source, onTap = onClick)

    Box(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                role = Role.Button
                onClick { onClick(); true }
            }
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = KeySpacing / 2, vertical = KeyRowSpacing / 2)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .keySurface(surface = { surface }, support = { shadowColor }, outline = outline),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

internal fun Modifier.keySurface(
    surface: () -> Color,
    support: () -> Color,
    cornerRadius: Dp = KeyCornerRadius,
    outline: Color? = null,
): Modifier = drawBehind {
    val supportPx = KeySupport.toPx()
    val radius = CornerRadius(cornerRadius.toPx())
    val capSize = Size(size.width, size.height - supportPx)

    if (outline == null) {
        drawRoundRect(
            color = support(),
            topLeft = Offset(0f, supportPx),
            size = capSize,
            cornerRadius = radius
        )
    }
    drawRoundRect(
        color = surface(),
        topLeft = Offset.Zero,
        size = capSize,
        cornerRadius = radius
    )
    if (outline != null) {
        val stroke = KeyOutlineWidth.toPx()
        drawRoundRect(
            color = outline,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(capSize.width - stroke, capSize.height - stroke),
            cornerRadius = radius,
            style = Stroke(width = stroke)
        )
    }
}

private val KeyOutlineWidth = 1.dp
