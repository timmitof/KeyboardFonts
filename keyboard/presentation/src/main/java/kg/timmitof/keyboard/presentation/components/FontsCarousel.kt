package kg.timmitof.keyboard.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import kg.timmitof.keyboard.domain.model.KeyboardFont
import kg.timmitof.keyboard.presentation.theme.KFTheme

/** Высота верхней панели (карусель шрифтов / подсказка режима курсора). */
internal val TopStripHeight = 40.dp

/**
 * Горизонтальная карусель шрифтов над клавишами
 */
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

    LazyRow(
        state = listState,
        modifier = modifier.height(TopStripHeight),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(fonts, key = { _, font -> font.id }) { index, font ->
            FontPill(
                preview = remember(font) { font.apply("Abc") },
                isSelected = index == selectedIndex,
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    val background by animateColorAsState(
        targetValue = if (isSelected) KFTheme.color.keyButtonPressedBackground else Color.Transparent,
        animationSpec = tween(200),
    )
    val textColor by animateColorAsState(
        targetValue = KFTheme.color.keyTextColor.copy(alpha = if (isSelected) 1f else 0.55f),
        animationSpec = tween(200),
    )

    Box(
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
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = preview,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
        )
    }
}
