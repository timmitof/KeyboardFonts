package kg.timmitof.keyboard.presentation.components.emoji

import kg.timmitof.keyboard.presentation.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kg.timmitof.keyboard.presentation.components.keys.KeyBase
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.theme.KFTheme

/**
 * Строка поиска эмодзи
 *
 * @param query текущий поисковый запрос.
 * @param results найденные эмодзи.
 * @param selectionChars сколько символов запроса выделено с конца (слайд по backspace).
 * @param onEvent проброс событий клавиатуры (запрос, выбор эмодзи, закрытие поиска).
 * @param emojiVariants варианты тона кожи по базовому эмодзи.
 * @param preferredVariants выбранные пользователем варианты (база → вариант).
 */
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchQueryField(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                query = query,
                selectionChars = selectionChars,
            )

            KeyBase(
                modifier = Modifier
                    .width(42.dp)
                    .fillMaxHeight(),
                background = KFTheme.color.keySpecialButtonBackground,
                shadowColor = KFTheme.color.keyButtonShadow,
                onClick = { onEvent(KeyboardEvent.OnEmojiSearchClose) }
            ) {
                Text(
                    text = "✕",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = KFTheme.color.keySpecialTextColor
                )
            }
        }

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
private fun SearchQueryField(
    query: String,
    modifier: Modifier = Modifier,
    selectionChars: Int = 0,
) {
    val textColor = KFTheme.color.keyTextColor
    val hasSelection = selectionChars in 1..query.length

    Row(
        modifier = modifier
            .background(KFTheme.color.keyButtonBackground, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_search_key),
            contentDescription = null,
            tint = KFTheme.color.keySpecialTextColor,
            modifier = Modifier.size(15.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                query.isEmpty() -> {
                    BlinkingCaret(color = textColor, resetKey = query)
                    Text(
                        text = stringResource(R.string.emoji_search_hint),
                        fontSize = 14.sp,
                        color = KFTheme.color.keySpecialTextColor
                    )
                }

                else -> {
                    Text(
                        text = if (hasSelection) {
                            buildAnnotatedString {
                                append(query.substring(0, query.length - selectionChars))
                                withStyle(SpanStyle(background = textColor.copy(alpha = 0.25f))) {
                                    append(query.substring(query.length - selectionChars))
                                }
                            }
                        } else {
                            AnnotatedString(query)
                        },
                        fontSize = 14.sp,
                        color = textColor,
                        maxLines = 1
                    )
                    if (!hasSelection) {
                        BlinkingCaret(color = textColor, resetKey = query)
                    }
                }
            }
        }
    }
}

@Composable
private fun BlinkingCaret(
    color: Color,
    resetKey: String,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(resetKey) {
        visible = true
        while (true) {
            delay(CaretBlinkMillis)
            visible = !visible
        }
    }

    Box(
        modifier = modifier
            .padding(horizontal = 1.dp)
            .width(2.dp)
            .height(18.dp)
            .background(if (visible) color else Color.Transparent)
    )
}

private const val CaretBlinkMillis = 500L

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