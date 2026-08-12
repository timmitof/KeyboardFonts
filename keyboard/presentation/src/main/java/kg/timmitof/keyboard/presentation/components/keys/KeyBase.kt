package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kg.timmitof.keyboard.presentation.components.KeyCornerRadius
import kg.timmitof.keyboard.presentation.components.KeyRowSpacing
import kg.timmitof.keyboard.presentation.components.KeySpacing
import kg.timmitof.keyboard.presentation.components.KeySupport
import kg.timmitof.keyboard.presentation.theme.KFTheme

/**
 * Основа любой клавиши: зона нажатия во всю ячейку ряда и видимая «шапка»
 * с опорой снизу вместо размытой тени.
 *
 * @param background цвет клавиши в покое.
 * @param pressedBackground цвет во время нажатия; по умолчанию — общая заливка нажатия темы.
 * @param customGestures свой набор жестов вместо обычного тапа (слайд по пробелу, пикер долгого нажатия).
 */
@Composable
internal fun KeyBase(
    modifier: Modifier = Modifier,
    background: Color,
    shadowColor: Color,
    pressedBackground: Color = KFTheme.color.keyButtonPressedBackground,
    interactionSource: MutableInteractionSource? = null,
    customGestures: ((MutableInteractionSource) -> Modifier)? = null,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val source = remember(interactionSource) { interactionSource ?: MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(80), label = "scale"
    )
    val surface by animateColorAsState(
        targetValue = if (isPressed) pressedBackground else background,
        animationSpec = tween(80), label = "surface"
    )

    val clickModifier = when {
        customGestures != null -> customGestures(source)

        else -> Modifier.clickable(
            interactionSource = source,
            indication = null,
            onClick = onClick
        )
    }

    Box(
        modifier = modifier.then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = KeySpacing / 2, vertical = KeyRowSpacing / 2)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .keySurface(surface = { surface }, support = { shadowColor }),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

private fun Modifier.keySurface(
    surface: () -> Color,
    support: () -> Color,
): Modifier = drawBehind {
    val supportPx = KeySupport.toPx()
    val radius = CornerRadius(KeyCornerRadius.toPx())
    val capSize = Size(size.width, size.height - supportPx)

    drawRoundRect(
        color = support(),
        topLeft = Offset(0f, supportPx),
        size = capSize,
        cornerRadius = radius
    )
    drawRoundRect(
        color = surface(),
        topLeft = Offset.Zero,
        size = capSize,
        cornerRadius = radius
    )
}
