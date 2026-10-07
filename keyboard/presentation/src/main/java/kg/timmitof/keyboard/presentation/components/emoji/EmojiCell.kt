package kg.timmitof.keyboard.presentation.components.emoji

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.presentation.theme.KFTheme

private val VariantIndicatorSize = 5.dp

private const val PressedScale = 1.25f

private val PressAnimationSpec = tween<Float>(durationMillis = 80)

@Composable
internal fun EmojiCell(
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variants: List<String> = emptyList(),
    onVariantSelect: (String) -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isPickerVisible by remember { mutableStateOf(false) }

    val isPressed by interactionSource.collectIsPressedAsState()
    // Значение читается только в graphicsLayer: анимация не рекомпозирует ячейку.
    val scale = animateFloatAsState(
        targetValue = if (isPressed) PressedScale else 1f,
        animationSpec = PressAnimationSpec,
        label = "emojiScale"
    )

    val hasVariants = variants.isNotEmpty()

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .then(if (hasVariants) Modifier.variantIndicator() else Modifier)
            .clip(CircleShape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = if (hasVariants) {
                    { isPickerVisible = true }
                } else {
                    null
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value },
            text = emoji,
            fontSize = 24.sp,
            maxLines = 1,
            softWrap = false,
        )

        if (isPickerVisible) {
            EmojiVariantPicker(
                variants = variants,
                selected = emoji,
                onSelect = { variant ->
                    isPickerVisible = false
                    onVariantSelect(variant)
                },
                onDismiss = { isPickerVisible = false }
            )
        }
    }
}

@Composable
private fun Modifier.variantIndicator(): Modifier {
    val color = KFTheme.color.keySpecialTextColor.copy(alpha = 0.4f)
    return drawWithCache {
        val indicator = VariantIndicatorSize.toPx()
        val path = Path().apply {
            moveTo(size.width, size.height - indicator)
            lineTo(size.width, size.height)
            lineTo(size.width - indicator, size.height)
            close()
        }
        onDrawBehind { drawPath(path, color) }
    }
}
