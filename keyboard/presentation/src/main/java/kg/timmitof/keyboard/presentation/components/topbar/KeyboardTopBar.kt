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
import androidx.compose.foundation.layout.fillMaxSize
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
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardOverlay
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(TopBarHeight)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedContent(
            targetState = mode,
            modifier = Modifier.weight(1f),
            transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
            label = "topBarContent"
        ) { current ->
            TopBarContent(mode = current, state = state, onEvent = onEvent)
        }

        AnimatedContent(
            targetState = mode == TopBarMode.FONTS_EXPANDED,
            transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
            label = "topBarAnchors"
        ) { isCollapsible ->
            TopBarAnchors(
                isCollapsible = isCollapsible,
                overlay = state.value.keyboardOverlay,
                onEvent = onEvent
            )
        }
    }
}

/**
 * Правый край панели.
 */
@Composable
private fun TopBarAnchors(
    isCollapsible: Boolean,
    overlay: KeyboardOverlay?,
    onEvent: (KeyboardEvent) -> Unit,
) {
    if (isCollapsible) {
        TopBarAnchorButton(
            iconRes = R.drawable.ic_close,
            contentDescription = stringResource(R.string.fonts_collapse),
            isActive = true,
            onClick = { onEvent(KeyboardEvent.OnFontsExpandedChange(false)) }
        )
        return
    }

    Row {
        KeyboardOverlay.entries.forEach { target ->
            TopBarAnchorButton(
                iconRes = target.iconRes,
                contentDescription = stringResource(target.titleRes),
                isActive = target == overlay,
                onClick = { onEvent(KeyboardEvent.OnOverlayChange(target.takeIf { it != overlay })) }
            )
        }
    }
}

private val KeyboardOverlay.iconRes: Int
    get() = when (this) {
        KeyboardOverlay.CLIPBOARD -> R.drawable.ic_clipboard
        KeyboardOverlay.QUICK_SETTINGS -> R.drawable.ic_settings_gear
    }

private val KeyboardOverlay.titleRes: Int
    get() = when (this) {
        KeyboardOverlay.CLIPBOARD -> R.string.clipboard_title
        KeyboardOverlay.QUICK_SETTINGS -> R.string.quick_settings_title
    }

@Composable
private fun TopBarContent(
    mode: TopBarMode,
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit,
) {
    when (mode) {
        TopBarMode.CURSOR -> CursorModeHint()

        TopBarMode.SUGGESTIONS -> ContentRow(spacing = 2.dp) {
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

        TopBarMode.FONTS_EXPANDED -> ContentRow(spacing = 8.dp) {
            FontsCarousel(
                fonts = state.value.fonts,
                selectedFontId = state.value.selectedFont.id,
                onFontSelect = { onEvent(KeyboardEvent.OnFontSelect(it)) },
                modifier = Modifier.weight(1f)
            )
        }

        TopBarMode.IDLE -> ContentRow {
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
private fun ContentRow(
    spacing: Dp = 6.dp,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun CursorModeHint() {
    Box(
        modifier = Modifier.fillMaxSize(),
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
