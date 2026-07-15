package kg.timmitof.keyboard.presentation.components.emoji

import KeyboardFonts.keyboard.keyboard.presentation.R
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.carouselItemEffect
import kg.timmitof.keyboard.domain.model.EmojiCategory
import kg.timmitof.keyboard.presentation.components.BackspaceKeyButton
import kg.timmitof.keyboard.presentation.components.SpecialKeyButton
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardEvent
import kg.timmitof.keyboard.presentation.theme.KFTheme
import kg.timmitof.keyboard.presentation.theme.KeyboardTheme

private const val SEARCH_TAB = 0
private const val RECENT_TAB = 1
private const val CATEGORY_OFFSET = 2

/**
 * Панель эмодзи.
 *
 * @param categories список категорий эмодзи.
 * @param recentEmojis недавно использованные эмодзи (свежие в начале).
 * @param onEvent проброс событий клавиатуры (выбор эмодзи, поиск, ABC, backspace).
 */
@Composable
internal fun EmojiPanel(
    categories: List<EmojiCategory>,
    recentEmojis: List<String>,
    onEvent: (KeyboardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedIndex by remember {
        mutableIntStateOf(if (recentEmojis.isNotEmpty()) RECENT_TAB else CATEGORY_OFFSET)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        AnimatedContent(
            targetState = selectedIndex,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            transitionSpec = {
                val towardsEnd = targetState > initialState
                (fadeIn(tween(150)) + slideInVertically(tween(200)) { it / 12 }) togetherWith
                        (fadeOut(tween(100)) + slideOutVertically(tween(200)) { if (towardsEnd) -it / 12 else it / 12 })
            },
            label = "Emoji category"
        ) { index ->
            val emojis = when (index) {
                RECENT_TAB -> recentEmojis
                else -> categories.getOrNull(index - CATEGORY_OFFSET)?.emojis.orEmpty()
            }

            if (index == RECENT_TAB && emojis.isEmpty()) {
                EmptyRecentPlaceholder()
            } else {
                EmojiGrid(
                    emojis = emojis,
                    onEmojiSelect = { onEvent(KeyboardEvent.OnEmojiSelect(it)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpecialKeyButton(
                label = "ABC",
                weight = 1.4f,
                onClick = { onEvent(KeyboardEvent.OnAbcSwitch) }
            )

            EmojiTabsCarousel(
                categories = categories,
                selectedIndex = selectedIndex,
                onTabClick = { index ->
                    if (index == SEARCH_TAB) {
                        onEvent(KeyboardEvent.OnEmojiSearchOpen)
                    } else {
                        selectedIndex = index
                    }
                },
                modifier = Modifier
                    .weight(6f)
                    .fillMaxHeight()
            )

            BackspaceKeyButton(
                weight = 1.4f,
                onClick = { onEvent(KeyboardEvent.OnBackspace) },
                onDeleteWord = { onEvent(KeyboardEvent.OnBackspaceDeleteWord) },
                onSelectChange = { onEvent(KeyboardEvent.OnBackspaceSelectChange(it)) },
                onSelectCommit = { onEvent(KeyboardEvent.OnBackspaceSelectCommit(it)) },
            )
        }
    }
}

/**
 * Карусель табов панели эмодзи
 */
@Composable
private fun EmojiTabsCarousel(
    categories: List<EmojiCategory>,
    selectedIndex: Int,
    onTabClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabIcons = remember(categories) {
        listOf("🔍", "🕐") + categories.map { it.icon }
    }
    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    ) {
        itemsIndexed(tabIcons) { index, icon ->
            EmojiCategoryTab(
                icon = icon,
                isSelected = index == selectedIndex,
                onClick = { onTabClick(index) },
                modifier = Modifier
                    .width(38.dp)
                    .fillParentMaxHeight()
                    .carouselItemEffect(listState, index)
            )
        }
    }
}

@Composable
private fun EmptyRecentPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.emoji_recent_empty),
            fontSize = 13.sp,
            color = KFTheme.color.keySpecialTextColor
        )
    }
}

@Composable
private fun EmojiGrid(
    emojis: List<String>,
    onEmojiSelect: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 42.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = emojis) { emoji ->
            EmojiCell(
                emoji = emoji,
                onClick = { onEmojiSelect(emoji) }
            )
        }
    }
}

@Composable
internal fun EmojiCell(
    emoji: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.25f else 1f,
        animationSpec = tween(80),
        label = "emoji-scale"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp,
            modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
        )
    }
}

@Composable
private fun EmojiCategoryTab(
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pillColor by animateColorAsState(
        targetValue = if (isSelected) KFTheme.color.keyButtonPressedBackground else Color.Transparent,
        animationSpec = tween(200),
        label = "tab-pill"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.55f,
        animationSpec = tween(200),
        label = "tab-alpha"
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

private val previewCategories = listOf(
    EmojiCategory(
        id = "smileys",
        icon = "😀",
        emojis = listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
            "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
            "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩"
        )
    ),
    EmojiCategory(id = "people", icon = "👋", emojis = listOf("👋", "🤚", "🖐", "✋")),
    EmojiCategory(id = "animals", icon = "🐻", emojis = listOf("🐶", "🐱", "🐭", "🐹")),
    EmojiCategory(id = "food", icon = "🍔", emojis = listOf("🍏", "🍎", "🍐", "🍊")),
    EmojiCategory(id = "activities", icon = "⚽", emojis = listOf("⚽", "🏀", "🏈", "⚾")),
    EmojiCategory(id = "travel", icon = "🚗", emojis = listOf("🚗", "🚕", "🚙", "🚌")),
    EmojiCategory(id = "objects", icon = "💡", emojis = listOf("⌚", "📱", "💻", "💡")),
    EmojiCategory(id = "symbols", icon = "❤️", emojis = listOf("❤️", "🧡", "💛", "💚")),
    EmojiCategory(id = "flags", icon = "🏁", emojis = listOf("🏁", "🚩", "🇰🇬", "🇰🇿")),
)

@Preview(showBackground = true)
@Composable
private fun EmojiPanelPreview() {
    KeyboardTheme {
        Box(modifier = Modifier.background(KFTheme.color.keyboardBackground)) {
            EmojiPanel(
                categories = previewCategories,
                recentEmojis = listOf("😂", "🔥", "❤️", "👍", "🎉"),
                onEvent = {},
                modifier = Modifier.height(216.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmojiPanelEmptyRecentPreview() {
    KeyboardTheme {
        Box(modifier = Modifier.background(KFTheme.color.keyboardBackground)) {
            EmojiPanel(
                categories = previewCategories,
                recentEmojis = emptyList(),
                onEvent = {},
                modifier = Modifier.height(216.dp)
            )
        }
    }
}
