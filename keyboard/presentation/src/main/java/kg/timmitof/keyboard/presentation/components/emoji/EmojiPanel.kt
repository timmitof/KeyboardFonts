package kg.timmitof.keyboard.presentation.components.emoji

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.domain.model.EmojiCategory
import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.presentation.components.KeyShape
import kg.timmitof.keyboard.presentation.components.KeyRowCount
import kg.timmitof.keyboard.presentation.components.KeyRowSpacing
import kg.timmitof.keyboard.presentation.components.LocalKeyRowHeight
import kg.timmitof.keyboard.presentation.components.TopBarHeight
import kg.timmitof.keyboard.presentation.components.keys.BackspaceKeyButton
import kg.timmitof.keyboard.presentation.components.keys.SpaceKeyButton
import kg.timmitof.keyboard.presentation.components.keys.SpecialKeyButton
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.theme.KFTheme
import kg.timmitof.keyboard.presentation.theme.KeyboardTheme
import kotlinx.coroutines.launch

private val SearchFieldHeight = 42.dp
private val TabsBarHeight = 34.dp
private val BottomRowHeight = 60.dp
private val SearchGridSpacing = 6.dp
private val GridTabsSpacing = 4.dp

private val EmojiChromeHeight =
    SearchFieldHeight + SearchGridSpacing + GridTabsSpacing + TabsBarHeight + BottomRowHeight

private const val PreferredGridRows = 5

private const val MinGridRows = 3

private const val MaxScreenFraction = 0.55f

/**
 * Сетка берёт целые ряды в пределах [MaxScreenFraction] экрана, но не меньше зоны ABC-слоя —
 * так панель не «съезжает» вниз при переключении и не режет ряд пополам.
 */
@Composable
internal fun EmojiPanel(
    categories: List<EmojiCategory>,
    recentEmojis: List<String>,
    onEvent: (KeyboardEvent) -> Unit,
    modifier: Modifier = Modifier,
    selectedLanguage: KeyboardLanguage? = null,
    emojiVariants: Map<String, List<String>> = emptyMap(),
    preferredVariants: Map<String, String> = emptyMap(),
) {
    val sections = remember(categories, recentEmojis) {
        buildEmojiSections(categories, recentEmojis)
    }
    val index = remember(sections) { sections.gridIndex() }

    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    val currentSection = remember(index) {
        derivedStateOf { index.sectionAt(gridState.firstVisibleItemIndex) }
    }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val keyboardHeight = TopBarHeight + KeyRowSpacing / 2 + LocalKeyRowHeight.current * KeyRowCount

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val gridHeight = remember(maxWidth, screenHeight, keyboardHeight) {
            gridHeight(
                cellSize = maxWidth / EmojiGridColumns,
                screenHeight = screenHeight,
                lettersHeight = keyboardHeight,
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            EmojiSearchField(
                onClick = { onEvent(KeyboardEvent.OnEmojiSearchOpen) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(SearchGridSpacing))

            val gridModifier = Modifier
                .height(gridHeight)
                .fillMaxWidth()

            if (sections.isEmpty()) {
                EmptyPlaceholder(modifier = gridModifier)
            } else {
                EmojiSectionsGrid(
                    sections = sections,
                    gridState = gridState,
                    currentSection = currentSection,
                    emojiVariants = emojiVariants,
                    preferredVariants = preferredVariants,
                    onEvent = onEvent,
                    modifier = gridModifier
                )
            }

            Spacer(modifier = Modifier.height(GridTabsSpacing))

            EmojiTabsBar(
                sections = sections,
                selectedIndex = currentSection.value,
                onTabClick = { section ->
                    scope.launch { gridState.animateScrollToItem(index.itemIndexOf(section)) }
                },
                modifier = Modifier.height(TabsBarHeight)
            )

            EmojiBottomRow(
                selectedLanguage = selectedLanguage,
                onEvent = onEvent
            )
        }
    }
}

private fun gridHeight(cellSize: Dp, screenHeight: Dp, lettersHeight: Dp): Dp {
    val minHeight = lettersHeight - EmojiChromeHeight
    if (cellSize <= 0.dp) return minHeight

    val available = screenHeight * MaxScreenFraction - EmojiChromeHeight - EmojiSectionHeaderHeight
    val rows = (available / cellSize).toInt().coerceIn(MinGridRows, PreferredGridRows)

    return (cellSize * rows + EmojiSectionHeaderHeight).coerceAtLeast(minHeight)
}

@Composable
private fun EmojiSearchField(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 3.dp)
            .height(SearchFieldHeight)
            .background(KFTheme.color.keyButtonBackground, KeyShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_search_key),
            contentDescription = null,
            tint = KFTheme.color.keySpecialTextColor,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = stringResource(R.string.emoji_search_hint),
            fontSize = 13.sp,
            color = KFTheme.color.keySpecialTextColor
        )
    }
}

@Composable
private fun EmojiBottomRow(
    selectedLanguage: KeyboardLanguage?,
    onEvent: (KeyboardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(BottomRowHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpecialKeyButton(
            label = "ABC",
            weight = 1.6f,
            onClick = { onEvent(KeyboardEvent.OnAbcSwitch) }
        )

        SpaceKeyButton(
            weight = 6.8f,
            languages = emptyList(),
            selectedLanguage = selectedLanguage,
            isLanguageSlideEnabled = false,
            onClick = { onEvent(KeyboardEvent.OnSpace) }
        )

        BackspaceKeyButton(
            weight = 1.6f,
            onClick = { onEvent(KeyboardEvent.OnBackspace) },
            onDeleteWord = { onEvent(KeyboardEvent.OnBackspaceDeleteWord) },
            onSelectChange = { onEvent(KeyboardEvent.OnBackspaceSelectChange(it)) },
            onSelectCommit = { onEvent(KeyboardEvent.OnBackspaceSelectCommit(it)) },
        )
    }
}

@Composable
private fun EmptyPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
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
                selectedLanguage = KeyboardLanguage(
                    code = "ru_ru",
                    displayName = "Русский",
                    shortName = "RU"
                ),
                emojiVariants = mapOf(
                    "👋" to listOf("👋", "👋🏻", "👋🏼", "👋🏽", "👋🏾", "👋🏿"),
                ),
                preferredVariants = mapOf("🤚" to "🤚🏿")
            )
        }
    }
}
