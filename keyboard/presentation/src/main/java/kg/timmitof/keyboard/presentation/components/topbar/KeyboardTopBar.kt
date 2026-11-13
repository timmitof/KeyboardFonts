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
import kg.timmitof.keyboard.font.domain.model.FontScript
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
import kg.timmitof.keyboard.presentation.components.fontSample
import kg.timmitof.keyboard.presentation.components.fontShortSample
import kg.timmitof.keyboard.presentation.components.orDefaultFor
import kg.timmitof.keyboard.presentation.components.LocalKeyboardMetrics
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardOverlay
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
import kg.timmitof.keyboard.presentation.screens.keyboard.states.SelectionTranslationStatus
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
            selectedFont = it.selectedFont.orDefaultFor(it.fontScript),
            fonts = it.availableFonts,
            fontScript = it.fontScript,
            suggestions = it.suggestions,
            noticeRes = it.noticeRes,
            overlay = it.keyboardOverlay,
            allowsTranslation = it.allowsTranslation,
            isTranslatorOpen = it.translator != null,
            isTranslatorHintPending = it.isTranslatorHintPending && it.allowsTranslation,
            selectionStatus = it.selectionTranslation
                ?.takeIf { selection -> selection.text == it.textContext.selected }
                ?.status,
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
                allowsTranslation = topBar.allowsTranslation,
                isTranslatorOpen = topBar.isTranslatorOpen,
                onEvent = onEvent
            )
        }
    }
}

@Composable
private fun TopBarAnchors(
    isCollapsible: Boolean,
    overlay: KeyboardOverlay?,
    allowsTranslation: Boolean,
    isTranslatorOpen: Boolean,
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
        // Переводчик — третий якорь, рядом с буфером и шестерёнкой; в паролях и адресах его нет.
        if (allowsTranslation) {
            TopBarAnchorButton(
                iconRes = R.drawable.ic_translate,
                contentDescription = stringResource(R.string.translator_title),
                isActive = isTranslatorOpen,
                onClick = { onEvent(KeyboardEvent.OnTranslatorToggle) }
            )
        }
        AnchorOverlays.forEach { target ->
            TopBarAnchorButton(
                iconRes = target.iconRes,
                contentDescription = stringResource(target.titleRes),
                isActive = target == overlay,
                onClick = { onEvent(KeyboardEvent.OnOverlayChange(target.takeIf { it != overlay })) }
            )
        }
    }
}

private val AnchorOverlays = KeyboardOverlay.entries.filter { it.isAnchor }

private val KeyboardOverlay.iconRes: Int
    get() = when (this) {
        KeyboardOverlay.CLIPBOARD -> R.drawable.ic_clipboard
        KeyboardOverlay.QUICK_SETTINGS -> R.drawable.ic_settings_gear
        KeyboardOverlay.TRANSLATION_TARGET, KeyboardOverlay.TRANSLATION_SOURCE -> R.drawable.ic_translate
    }

private val KeyboardOverlay.titleRes: Int
    get() = when (this) {
        KeyboardOverlay.CLIPBOARD -> R.string.clipboard_title
        KeyboardOverlay.QUICK_SETTINGS -> R.string.quick_settings_title
        KeyboardOverlay.TRANSLATION_TARGET -> R.string.translator_target_title
        KeyboardOverlay.TRANSLATION_SOURCE -> R.string.translator_source_title
    }

@Composable
private fun TopBarContent(
    mode: TopBarMode,
    slice: TopBarSlice,
    onEvent: (KeyboardEvent) -> Unit,
) {
    val fontPreview = remember(slice.selectedFont, slice.fontScript) {
        slice.selectedFont.apply(slice.fontScript.fontShortSample)
    }

    when (mode) {
        TopBarMode.CURSOR -> CursorModeHint()

        TopBarMode.SELECTION -> ContentRow {
            TopBarPill(
                label = stringResource(slice.selectionStatus.labelRes),
                isActive = true,
                isEnabled = slice.selectionStatus != SelectionTranslationStatus.TRANSLATING,
                onClick = { onEvent(KeyboardEvent.OnTranslateSelection) },
            )
            Box(modifier = Modifier.weight(1f))
        }

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
                sample = slice.fontScript.fontSample,
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

            // Один раз, пока переводчик ни разу не открывали: рядом с его кнопкой справа.
            if (slice.isTranslatorHintPending) {
                TopBarPill(
                    label = stringResource(R.string.translator_new),
                    isActive = true,
                    onClick = { onEvent(KeyboardEvent.OnTranslatorToggle) },
                )
            }
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
    val fontScript: FontScript,
    val suggestions: List<WordSuggestion>,
    @param:StringRes val noticeRes: Int?,
    val overlay: KeyboardOverlay?,
    val allowsTranslation: Boolean,
    val isTranslatorOpen: Boolean,
    val isTranslatorHintPending: Boolean,
    /** Что с переводом выделенного; null — ещё не нажимали. */
    val selectionStatus: SelectionTranslationStatus?,
)

private enum class TopBarMode { CURSOR, SELECTION, SUGGESTIONS, FONTS_EXPANDED, IDLE }

private val KeyboardState.topBarMode: TopBarMode
    get() = when {
        isCursorMode -> TopBarMode.CURSOR
        // Выделили текст — подсказки к нему ни к чему, а перевести его — как раз к месту.
        canTranslateSelection -> TopBarMode.SELECTION
        isFontsExpanded -> TopBarMode.FONTS_EXPANDED
        hasSuggestions -> TopBarMode.SUGGESTIONS
        else -> TopBarMode.IDLE
    }

/** Подпись кнопки перевода выделенного — то, что сейчас происходит с переводом. */
private val SelectionTranslationStatus?.labelRes: Int
    get() = when (this) {
        null -> R.string.translator_selection
        SelectionTranslationStatus.TRANSLATING -> R.string.translator_selection_working
        SelectionTranslationStatus.DOWNLOADING -> R.string.translator_selection_downloading
        SelectionTranslationStatus.UNSUPPORTED -> R.string.translator_selection_unsupported
        SelectionTranslationStatus.FAILED -> R.string.translator_selection_failed
    }

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
