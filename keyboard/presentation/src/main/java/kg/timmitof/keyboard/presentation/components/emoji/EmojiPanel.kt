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
import kg.timmitof.keyboard.presentation.components.keys.BackspaceKeyButton
import kg.timmitof.keyboard.presentation.components.keys.SpecialKeyButton
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.theme.KFTheme
import kg.timmitof.keyboard.presentation.theme.KeyboardTheme

/**
 * Панель эмодзи.
 */
@Composable
internal fun EmojiPanel(
    categories: List<EmojiCategory>,
    recentEmojis: List<String>,
    onEvent: (KeyboardEvent) -> Unit,
    modifier: Modifier = Modifier,
    emojiVariants: Map<String, List<String>> = emptyMap(),
    preferredVariants: Map<String, String> = emptyMap(),
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
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            tabs = tabs,
            selectedIndex = selectedIndex,
            recentEmojis = recentEmojis,
            emojiVariants = emojiVariants,
            preferredVariants = preferredVariants,
            onEvent = onEvent,
        )

        Spacer(modifier = Modifier.height(6.dp))

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
    modifier: Modifier = Modifier,
    tabs: List<EmojiTab>,
    selectedIndex: Int,
    recentEmojis: List<String>,
    emojiVariants: Map<String, List<String>>,
    preferredVariants: Map<String, String>,
    onEvent: (KeyboardEvent) -> Unit,
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
                emojiVariants = emojiVariants,
                preferredVariants = preferredVariants,
                onEvent = onEvent
            )

            EmojiTab.Recent -> if (recentEmojis.isEmpty()) {
                EmptyRecentPlaceholder()
            } else {
                EmojiGrid(
                    emojis = recentEmojis,
                    onEvent = onEvent
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

@Composable
private fun EmojiGrid(
    emojis: List<String>,
    onEvent: (KeyboardEvent) -> Unit,
    emojiVariants: Map<String, List<String>> = emptyMap(),
    preferredVariants: Map<String, String> = emptyMap(),
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 42.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = emojis, key = { it }) { base ->
            val displayed = preferredVariants[base] ?: base
            EmojiCell(
                emoji = displayed,
                variants = emojiVariants[base].orEmpty(),
                onClick = { onEvent(KeyboardEvent.OnEmojiSelect(displayed)) },
                onVariantSelect = { variant ->
                    onEvent(KeyboardEvent.OnEmojiVariantSelect(base, variant))
                }
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
                modifier = Modifier.height(216.dp),
                emojiVariants = mapOf(
                    "👋" to listOf("👋", "👋🏻", "👋🏼", "👋🏽", "👋🏾", "👋🏿"),
                    "🤚" to listOf("🤚", "🤚🏻", "🤚🏼", "🤚🏽", "🤚🏾", "🤚🏿"),
                ),
                preferredVariants = mapOf("🤚" to "🤚🏿")
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
