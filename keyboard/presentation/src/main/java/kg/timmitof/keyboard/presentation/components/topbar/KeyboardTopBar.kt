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
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kg.timmitof.keyboard.font.domain.model.KeyboardFont
import kg.timmitof.keyboard.suggestion.domain.model.WordSuggestion
import kg.timmitof.keyboard.presentation.screens.keyboard.rememberSlice
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
import kg.timmitof.keyboard.presentation.components.LocalKeyboardMetrics
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardOverlay
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
import kg.timmitof.keyboard.presentation.theme.KFTheme

@Composable
internal fun KeyboardTopBar(
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val topBar by state.rememberSlice {
        TopBarSlice(
            mode = it.topBarMode,
            allowsFonts = it.allowsFonts,
            selectedFont = it.selectedFont,
            fonts = it.fonts,
            suggestions = it.suggestions,
            noticeRes = it.noticeRes,
            overlay = it.keyboardOverlay,
        )
    }
    val mode = topBar.mode

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(LocalKeyboardMetrics.current.topBarHeight)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedContent(
            targetState = mode,
            modifier = Modifier.weight(1f),
            transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
            label = "topBarContent"
        ) { current ->
            TopBarContent(mode = current, slice = topBar, onEvent = onEvent)
        }

        AnimatedContent(
            targetState = mode == TopBarMode.FONTS_EXPANDED,
            transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
            label = "topBarAnchors"
        ) { isCollapsible ->
            TopBarAnchors(
                isCollapsible = isCollapsible,
                overlay = topBar.overlay,
                onEvent = onEvent
            )
        }
    }
}

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
    slice: TopBarSlice,
    onEvent: (KeyboardEvent) -> Unit,
) {
    val fontPreview = remember(slice.selectedFont) { slice.selectedFont.apply(FontPreviewText) }

    when (mode) {
        TopBarMode.CURSOR -> CursorModeHint()

        TopBarMode.SUGGESTIONS -> ContentRow(spacing = 2.dp) {
            if (slice.allowsFonts) {
                FontToggleButton(
                    preview = fontPreview,
                    onClick = { onEvent(KeyboardEvent.OnFontsExpandedChange(true)) }
                )
            }
            SuggestionsRow(
                suggestions = slice.suggestions,
                onSelect = { onEvent(KeyboardEvent.OnSuggestionSelect(it)) },
                modifier = Modifier.weight(1f)
            )
        }

        TopBarMode.FONTS_EXPANDED -> ContentRow(spacing = 8.dp) {
            FontsCarousel(
                fonts = slice.fonts,
                selectedFontId = slice.selectedFont.id,
                onFontSelect = { onEvent(KeyboardEvent.OnFontSelect(it)) },
                modifier = Modifier.weight(1f)
            )
        }

        TopBarMode.IDLE -> ContentRow {
            if (slice.allowsFonts) {
                FontToggleButton(
                    preview = fontPreview,
                    onClick = { onEvent(KeyboardEvent.OnFontsExpandedChange(true)) }
                )
            }
            slice.noticeRes?.let { NoticePill(textRes = it) }

            Box(modifier = Modifier.weight(1f))
        }
    }
}

/** Всё, что читает топбар; data class — срез сравнивается по значению. */
@Immutable
private data class TopBarSlice(
    val mode: TopBarMode,
    val allowsFonts: Boolean,
    val selectedFont: KeyboardFont,
    val fonts: List<KeyboardFont>,
    val suggestions: List<WordSuggestion>,
    @param:StringRes val noticeRes: Int?,
    val overlay: KeyboardOverlay?,
)

private enum class TopBarMode { CURSOR, SUGGESTIONS, FONTS_EXPANDED, IDLE }

private val KeyboardState.topBarMode: TopBarMode
    get() = when {
        isCursorMode -> TopBarMode.CURSOR
        isFontsExpanded -> TopBarMode.FONTS_EXPANDED
        hasSuggestions -> TopBarMode.SUGGESTIONS
        else -> TopBarMode.IDLE
    }

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
