package kg.timmitof.keyboard.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.font.domain.model.KeyboardFont
import kg.timmitof.keyboard.presentation.theme.KFTheme

private val FontPillHeight = 32.dp

@Composable
internal fun FontsCarousel(
    fonts: List<KeyboardFont>,
    selectedFontId: String,
    onFontSelect: (KeyboardFont) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val selectedIndex = remember(fonts, selectedFontId) {
        fonts.indexOfFirst { it.id == selectedFontId }.coerceAtLeast(0)
    }

    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(selectedIndex)
    }

    LazyRow(
        state = listState,
        modifier = modifier.height(FontPillHeight),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(fonts, key = { _, font -> font.id }) { index, font ->
            val isSelected = index == selectedIndex
            FontPill(
                preview = remember(font) { font.apply("Abc") },
                isSelected = isSelected,
                isResettable = isSelected && !font.isDefault,
                onClick = { onFontSelect(font) },
                modifier = Modifier.fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun FontPill(
    preview: String,
    isSelected: Boolean,
    isResettable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(FontPillHeight / 2)
    // Выбранный шрифт — плашка цвета служебных клавиш; остальные — текст прямо на фоне.
    val background by animateColorAsState(
        targetValue = if (isSelected) KFTheme.color.keySpecialButtonBackground else Color.Transparent,
        animationSpec = tween(200),
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) {
            KFTheme.color.keySpecialLabelColor
        } else {
            KFTheme.color.keyTextColor.copy(alpha = UnselectedAlpha)
        },
        animationSpec = tween(200),
    )

    Row(
        modifier = modifier
            .clip(shape)
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .defaultMinSize(minWidth = 52.dp)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = preview,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
        )

        AnimatedVisibility(
            visible = isResettable,
            enter = ResetEnterTransition,
            exit = ResetExitTransition,
        ) {
            Text(
                modifier = Modifier.padding(start = 6.dp),
                text = "✕",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
            )
        }
    }
}

private const val UnselectedAlpha = 0.55f

private val ResetEnterTransition =
    fadeIn(tween(180)) + expandHorizontally(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        expandFrom = Alignment.Start,
    )

private val ResetExitTransition =
    fadeOut(tween(120)) + shrinkHorizontally(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        shrinkTowards = Alignment.Start,
    )
