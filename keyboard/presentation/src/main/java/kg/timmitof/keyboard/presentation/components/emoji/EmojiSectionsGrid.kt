package kg.timmitof.keyboard.presentation.components.emoji

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.theme.KFTheme

internal const val EmojiGridColumns = 8

internal val EmojiSectionHeaderHeight = 28.dp

@Immutable
internal data class EmojiGridIndex(
    private val sectionStarts: List<Int>,
) {
    fun itemIndexOf(section: Int): Int = sectionStarts.getOrElse(section) { 0 }

    fun sectionAt(itemIndex: Int): Int =
        sectionStarts.indexOfLast { it <= itemIndex }.coerceAtLeast(0)
}

/** Заголовок в плоском списке — один элемент, поэтому смещение секции = заголовок + её эмодзи. */
internal fun List<EmojiSection>.gridIndex(): EmojiGridIndex {
    var offset = 0
    val starts = map { section ->
        val start = offset
        offset += 1 + section.emojis.size
        start
    }
    return EmojiGridIndex(starts)
}

@Composable
internal fun EmojiSectionsGrid(
    sections: List<EmojiSection>,
    gridState: LazyGridState,
    currentSection: State<Int>,
    emojiVariants: Map<String, List<String>>,
    preferredVariants: Map<String, String>,
    onEvent: (KeyboardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(EmojiGridColumns),
            state = gridState,
            modifier = Modifier.fillMaxSize()
        ) {
            sections.forEach { section ->
                item(
                    key = "header:${section.id}",
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    SectionHeader(titleRes = section.titleRes)
                }

                items(
                    items = section.emojis,
                    key = { emoji -> "${section.id}:$emoji" }
                ) { base ->
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

        sections.getOrNull(currentSection.value)?.let { section ->
            SectionHeader(
                titleRes = section.titleRes,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KFTheme.color.keyboardBackground)
            )
        }
    }
}

@Composable
private fun SectionHeader(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(EmojiSectionHeaderHeight)
            .padding(start = 6.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Text(
            modifier = Modifier.padding(bottom = 6.dp),
            text = stringResource(titleRes),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.9.sp,
            color = KFTheme.color.keySpecialTextColor
        )
    }
}
