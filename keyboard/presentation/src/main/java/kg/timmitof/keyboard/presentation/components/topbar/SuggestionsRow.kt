package kg.timmitof.keyboard.presentation.components.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.suggestion.domain.model.WordSuggestion
import kg.timmitof.keyboard.presentation.theme.KFTheme

/**
 * Строка подсказок слов.
 */
@Composable
internal fun SuggestionsRow(
    suggestions: List<WordSuggestion>,
    onSelect: (WordSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visible = remember(suggestions) { suggestions.take(MaxSuggestions) }

    Row(
        modifier = modifier.fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        visible.forEachIndexed { index, suggestion ->
            if (index > 0) SuggestionDivider()

            SuggestionSlot(
                suggestion = suggestion,
                onClick = { onSelect(suggestion) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SuggestionSlot(
    suggestion: WordSuggestion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = when {
        suggestion.isAutoCorrect -> KFTheme.color.keyAccentBackground
        suggestion.isLiteral -> KFTheme.color.keySpecialTextColor
        else -> KFTheme.color.keyTextColor
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (suggestion.isLiteral) "«${suggestion.text}»" else suggestion.text,
            fontSize = 15.sp,
            fontWeight = if (suggestion.isAutoCorrect || !suggestion.isLiteral) {
                FontWeight.Medium
            } else {
                FontWeight.Normal
            },
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SuggestionDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(20.dp)
            .background(KFTheme.color.keyTextColor.copy(alpha = 0.16f))
    )
}

private const val MaxSuggestions = 3
