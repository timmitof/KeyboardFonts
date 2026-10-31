package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
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
    /** Принимает источник и [onPress] — его жест должен вызвать сразу на касание (вибро/звук). */
    customGestures: ((MutableInteractionSource, onPress: () -> Unit) -> Modifier)? = null,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val source = remember(interactionSource) { interactionSource ?: MutableInteractionSource() }

    // Вибрация и звук — прямо из жеста на касание: через состояние они ждали бы кадр, а короткий тап терялся.
    val feedback by rememberUpdatedState(LocalKeyFeedback.current)
    val currentSound by rememberUpdatedState(sound)
    val onPress = remember { { feedback.onKeyPress(currentSound) } }

    // Доля нажатия 0..1 анимируется из потока взаимодействий и читается только при отрисовке: нажатие не рекомпозирует клавишу.
    val pressFraction = remember { Animatable(0f) }
    LaunchedEffect(source) {
        val presses = mutableListOf<PressInteraction.Press>()
        source.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> presses += interaction
                is PressInteraction.Release -> presses -= interaction.press
                is PressInteraction.Cancel -> presses -= interaction.press
            }
            val target = if (presses.isEmpty()) 0f else 1f
            launch { pressFraction.animateTo(target, PressAnimationSpec) }
        }
    }

    val outline = KFTheme.color.keyOutline.takeIf { KFTheme.isKeyOutlined }

    val clickModifier = customGestures?.invoke(source, onPress)
        ?: Modifier.keyClickable(interactionSource = source, onPress = onPress, onTap = onClick)

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
                .graphicsLayer {
                    val scale = lerp(1f, PressedScale, pressFraction.value)
                    scaleX = scale
                    scaleY = scale
                }
                .keySurface(
                    surface = { lerp(background, pressedBackground, pressFraction.value) },
                    support = { shadowColor },
                    outline = outline
                ),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

/**
 * [Modifier.zIndex] через лямбду: состояние читается на фазе размещения, без рекомпозиции клавиши.
 */
internal fun Modifier.liftedZIndex(lifted: () -> Boolean): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.place(0, 0, zIndex = if (lifted()) 1f else 0f)
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

private const val PressedScale = 0.92f

private val PressAnimationSpec = tween<Float>(durationMillis = 80)
