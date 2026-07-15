package kg.timmitof.keyboard.presentation.components.emoji

import KeyboardFonts.keyboard.keyboard.presentation.R
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.domain.model.EmojiCategory
import kg.timmitof.keyboard.presentation.components.BackspaceKeyButton
import kg.timmitof.keyboard.presentation.components.SpecialKeyButton
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardEvent
import kg.timmitof.keyboard.presentation.theme.KFTheme
import kg.timmitof.keyboard.presentation.theme.KeyboardTheme

/** Высота нижней панели навигации. */
private val BottomBarHeight = 42.dp

/** Отступ между контентом, нижней панелью и её кнопками. */
private val PanelSpacing = 6.dp

/** Минимальный размер ячейки эмодзи в сетке. */
private val EmojiCellMinSize = 42.dp

/** Вес боковых кнопок нижней панели (ABC, backspace). */
private const val SideKeyWeight = 1.4f

/** Вес карусели табов в нижней панели. */
private const val CarouselWeight = 6f

/**
 * Панель эмодзи.
 */
@Composable
internal fun EmojiPanel(
    categories: List<EmojiCategory>,
    recentEmojis: List<String>,
    onEvent: (KeyboardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = remember(categories) {
        listOf(EmojiTab.Search, EmojiTab.Recent) + categories.map { EmojiTab.Category(it) }
    }
    var selectedIndex by remember {
        mutableIntStateOf(
            if (recentEmojis.isNotEmpty()) {
                tabs.indexOf(EmojiTab.Recent)
            } else {
                tabs.indexOfFirst { it is EmojiTab.Category }.coerceAtLeast(0)
            }
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        EmojiTabContent(
            tabs = tabs,
            selectedIndex = selectedIndex,
            recentEmojis = recentEmojis,
            onEvent = onEvent,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(PanelSpacing))

        EmojiBottomBar(
            tabs = tabs,
            selectedIndex = selectedIndex,
            onTabSelect = { selectedIndex = it },
            onEvent = onEvent
        )
    }
}

@Composable
private fun EmojiTabContent(
    tabs: List<EmojiTab>,
    selectedIndex: Int,
    recentEmojis: List<String>,
    onEvent: (KeyboardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = selectedIndex,
        modifier = modifier,
        transitionSpec = {
            val towardsEnd = targetState > initialState
            (fadeIn(tween(150)) + slideInVertically(tween(200)) { it / 12 }) togetherWith
                    (fadeOut(tween(100)) + slideOutVertically(tween(200)) { if (towardsEnd) -it / 12 else it / 12 })
        },
        label = "Emoji category"
    ) { index ->
        when (val tab = tabs.getOrNull(index)) {
            is EmojiTab.Category -> EmojiGrid(
                emojis = tab.category.emojis,
                onEmojiSelect = { onEvent(KeyboardEvent.OnEmojiSelect(it)) }
            )

            EmojiTab.Recent -> if (recentEmojis.isEmpty()) {
                EmptyRecentPlaceholder()
            } else {
                EmojiGrid(
                    emojis = recentEmojis,
                    onEmojiSelect = { onEvent(KeyboardEvent.OnEmojiSelect(it)) }
                )
            }
            else -> Unit
        }
    }
}

@Composable
private fun EmojiBottomBar(
    tabs: List<EmojiTab>,
    selectedIndex: Int,
    onTabSelect: (Int) -> Unit,
    onEvent: (KeyboardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(BottomBarHeight),
        horizontalArrangement = Arrangement.spacedBy(PanelSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpecialKeyButton(
            label = "ABC",
            weight = SideKeyWeight,
            onClick = { onEvent(KeyboardEvent.OnAbcSwitch) }
        )

        EmojiTabsCarousel(
            tabs = tabs,
            selectedIndex = selectedIndex,
            onTabClick = { index ->
                if (tabs[index] == EmojiTab.Search) {
                    onEvent(KeyboardEvent.OnEmojiSearchOpen)
                } else {
                    onTabSelect(index)
                }
            },
            modifier = Modifier
                .weight(CarouselWeight)
                .fillMaxHeight()
        )

        BackspaceKeyButton(
            weight = SideKeyWeight,
            onClick = { onEvent(KeyboardEvent.OnBackspace) },
            onDeleteWord = { onEvent(KeyboardEvent.OnBackspaceDeleteWord) },
            onSelectChange = { onEvent(KeyboardEvent.OnBackspaceSelectChange(it)) },
            onSelectCommit = { onEvent(KeyboardEvent.OnBackspaceSelectCommit(it)) },
        )
    }
}

@Composable
private fun EmojiGrid(
    emojis: List<String>,
    onEmojiSelect: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = EmojiCellMinSize),
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
