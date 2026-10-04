package kg.timmitof.core.ui.components.tabs

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.theme.AccentRole
import kg.timmitof.core.ui.theme.appColors

@Immutable
data class ChipTab<K>(
    val key: K,
    val label: String,
    val icon: Painter,
    val role: AccentRole,
)

@Composable
fun <K> ChipTabs(
    tabs: List<ChipTab<K>>,
    selected: K,
    onSelect: (K) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selected) {
        val index = tabs.indexOfFirst { it.key == selected }
        if (index >= 0) listState.animateScrollToItem(index = (index - 1).coerceAtLeast(0))
    }

    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .fadingEnd { listState.canScrollForward },
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(items = tabs, key = { it.key.toString() }) { tab ->
            ChipTabItem(
                tab = tab,
                isSelected = tab.key == selected,
                onClick = { onSelect(tab.key) },
            )
        }
    }
}

@Composable
private fun <K> ChipTabItem(
    tab: ChipTab<K>,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.appColors
    val tones = colors[tab.role]

    val background by animateColorAsState(
        targetValue = if (isSelected) colors.selected else colors.cardMuted,
        animationSpec = spring(stiffness = 600f),
        label = "chipBackground",
    )
    val content by animateColorAsState(
        targetValue = if (isSelected) colors.onSelected else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = 600f),
        label = "chipContent",
    )

    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(CircleShape)
            .drawBehind { drawRect(background) }
            .clickable(onClick = onClick)
            .padding(start = 6.dp, end = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(tones.container),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = tab.icon,
                contentDescription = null,
                tint = tones.solid,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = tab.label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = content,
            maxLines = 1,
        )
    }
}

/** Гасит правый край, пока [isActive]; условие читается в draw-фазе, поэтому прокрутка не вызывает рекомпозиций. */
private fun Modifier.fadingEnd(isActive: () -> Boolean): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        if (isActive()) {
            drawRect(
                brush = Brush.horizontalGradient(
                    FadeStart to Color.Black,
                    1f to Color.Transparent,
                ),
                blendMode = BlendMode.DstIn,
            )
        }
    }

private const val FadeStart = 0.82f
