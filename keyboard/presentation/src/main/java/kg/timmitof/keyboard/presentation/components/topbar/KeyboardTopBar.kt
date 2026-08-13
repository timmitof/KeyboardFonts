package kg.timmitof.keyboard.presentation.components.topbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.presentation.components.FontsCarousel
import kg.timmitof.keyboard.presentation.components.TopBarHeight
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
import kg.timmitof.keyboard.presentation.theme.KFTheme

/**
 * Верхняя панель клавиатуры.
 */
@Composable
internal fun KeyboardTopBar(
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val mode = state.value.topBarMode

    AnimatedContent(
        targetState = mode,
        modifier = modifier
            .fillMaxWidth()
            .height(TopBarHeight),
        transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
        label = "topBar"
    ) { current ->
        when (current) {
            TopBarMode.CURSOR -> CursorModeHint()

            TopBarMode.SUGGESTIONS -> TopBarRow {
                if (state.value.allowsFonts) {
                    FontToggleButton(
                        preview = state.value.selectedFont.apply(FontPreviewText),
                        onClick = { onEvent(KeyboardEvent.OnFontsExpandedChange(true)) }
                    )
                }
                SuggestionsRow(
                    suggestions = state.value.suggestions,
                    onSelect = { onEvent(KeyboardEvent.OnSuggestionSelect(it)) },
                    modifier = Modifier.weight(1f)
                )
            }

            TopBarMode.FONTS_EXPANDED -> TopBarRow(spacing = 8.dp) {
                FontsCarousel(
                    fonts = state.value.fonts,
                    selectedFontId = state.value.selectedFont.id,
                    onFontSelect = { onEvent(KeyboardEvent.OnFontSelect(it)) },
                    modifier = Modifier.weight(1f)
                )
                TopBarIconButton(
                    iconRes = R.drawable.ic_close_small,
                    contentDescription = stringResource(R.string.fonts_collapse),
                    onClick = { onEvent(KeyboardEvent.OnFontsExpandedChange(false)) }
                )
            }

            TopBarMode.IDLE -> TopBarRow {
                if (state.value.allowsFonts) {
                    FontToggleButton(
                        preview = state.value.selectedFont.apply(FontPreviewText),
                        onClick = { onEvent(KeyboardEvent.OnFontsExpandedChange(true)) }
                    )
                }
                state.value.noticeRes?.let { NoticePill(textRes = it) }

                Box(modifier = Modifier.weight(1f))
            }
        }
    }
}

private enum class TopBarMode { CURSOR, SUGGESTIONS, FONTS_EXPANDED, IDLE }

private val KeyboardState.topBarMode: TopBarMode
    get() = when {
        isCursorMode -> TopBarMode.CURSOR
        isFontsExpanded -> TopBarMode.FONTS_EXPANDED
        hasSuggestions -> TopBarMode.SUGGESTIONS
        else -> TopBarMode.IDLE
    }

/** Текст на кнопке шрифтов — показывает выбранный стиль. */
private const val FontPreviewText = "Aa"

@Composable
private fun TopBarRow(
    spacing: Dp = 6.dp,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(TopBarHeight)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun CursorModeHint() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(TopBarHeight),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.cursor_mode_hint),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = KFTheme.color.keySpecialTextColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
