package kg.timmitof.keyboard.presentation.components.emoji

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.carouselItemEffect
import kg.timmitof.keyboard.domain.model.EmojiCategory
import kg.timmitof.keyboard.presentation.theme.KFTheme



/** Ширина одного таба карусели. */
private val TabWidth = 38.dp

@Composable
internal fun EmojiTabsCarousel(
    tabs: List<EmojiTab>,
    selectedIndex: Int,
    onTabClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    ) {
        itemsIndexed(tabs) { index, tab ->
            EmojiTabItem(
                icon = tab.icon,
                isSelected = index == selectedIndex,
                onClick = { onTabClick(index) },
                modifier = Modifier
                    .width(TabWidth)
                    .fillParentMaxHeight()
                    .carouselItemEffect(listState, index)
            )
        }
    }
}

@Composable
private fun EmojiTabItem(
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pillColor by animateColorAsState(
        targetValue = if (isSelected) KFTheme.color.keyButtonPressedBackground else Color.Transparent,
        animationSpec = tween(200),
    )
    val alpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.55f,
        animationSpec = tween(200),
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(pillColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = 18.sp,
            modifier = Modifier.graphicsLayer { this.alpha = alpha }
        )
    }
}
