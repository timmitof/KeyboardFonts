package kg.timmitof.keyboard.presentation.components.emoji

import kg.timmitof.keyboard.presentation.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.presentation.components.InlineSearchBar
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.theme.KFTheme

@Composable
internal fun EmojiSearchBar(
    modifier: Modifier = Modifier,
    query: String,
    results: List<String>,
    selectionChars: Int = 0,
    onEvent: (KeyboardEvent) -> Unit,
    emojiVariants: Map<String, List<String>> = emptyMap(),
    preferredVariants: Map<String, String> = emptyMap(),
) {
    Column(modifier = modifier.fillMaxWidth()) {
        InlineSearchBar(
            query = query,
            hint = stringResource(R.string.emoji_search_hint),
            selectionChars = selectionChars,
            onClose = { onEvent(KeyboardEvent.OnEmojiSearchClose) },
        )

        Spacer(modifier = Modifier.height(6.dp))

        SearchResultsRow(
            query = query,
            results = results,
            emojiVariants = emojiVariants,
            preferredVariants = preferredVariants,
            onEvent = onEvent,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        )
    }
}

@Composable
private fun SearchResultsRow(
    query: String,
    results: List<String>,
    emojiVariants: Map<String, List<String>>,
    preferredVariants: Map<String, String>,
    onEvent: (KeyboardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        results.isNotEmpty() -> LazyRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(items = results, key = { it }) { base ->
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

        else -> Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (query.isEmpty()) {
                    stringResource(R.string.emoji_search_start_typing)
                } else {
                    stringResource(R.string.emoji_search_no_results)
                },
                fontSize = 13.sp,
                color = KFTheme.color.keySpecialTextColor
            )
        }
    }
}