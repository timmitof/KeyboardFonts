package kg.timmitof.keyboard.presentation.components.overlay.clipboard

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.domain.model.ClipboardEntry
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.presentation.components.KeyShape
import kg.timmitof.keyboard.presentation.components.KeySupport
import kg.timmitof.keyboard.presentation.components.overlay.OverlayActionRow
import kg.timmitof.keyboard.presentation.components.overlay.OverlayHeader
import kg.timmitof.keyboard.presentation.components.overlay.OverlayIconButton
import kg.timmitof.keyboard.presentation.screens.keyboard.states.ClipboardAction
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
import kg.timmitof.keyboard.presentation.theme.KFTheme

/** Буфер обмена — то же окно, что и быстрые настройки, отличается только содержимым. */
@Composable
internal fun ColumnScope.ClipboardOverlay(
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit,
) {
    val board = state.value.clipboard
    val close = { onEvent(KeyboardEvent.OnOverlayChange(null)) }

    OverlayHeader(
        title = stringResource(R.string.clipboard_title),
        onClose = close,
    ) {
        if (board.hasClearable) {
            OverlayIconButton(
                iconRes = R.drawable.ic_trash,
                contentDescription = stringResource(R.string.clipboard_clear),
                onClick = { onEvent(KeyboardEvent.OnClipboardAction(ClipboardAction.ClearRecent)) }
            )
        }
    }

    if (board.isEmpty) {
        EmptyClipboard(modifier = Modifier.weight(1f))
    } else {
        val pinnedTitle = stringResource(R.string.clipboard_pinned)
        val recentTitle = stringResource(R.string.clipboard_recent)

        LazyVerticalGrid(
            columns = GridCells.Fixed(ClipboardColumns),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            section(title = pinnedTitle, entries = board.pinned, onEvent = onEvent)
            section(title = recentTitle, entries = board.recent, onEvent = onEvent)
        }
    }

    OverlayActionRow(
        label = stringResource(R.string.clipboard_open_app),
        onClick = { onEvent(KeyboardEvent.OnOpenApp) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
    )
}

private fun LazyGridScope.section(
    title: String,
    entries: List<ClipboardEntry>,
    onEvent: (KeyboardEvent) -> Unit,
) {
    if (entries.isEmpty()) return

    item(span = { GridItemSpan(maxLineSpan) }) { SectionHeader(title = title) }

    items(entries, key = ClipboardEntry::id) { entry ->
        ClipboardCard(entry = entry, onEvent = onEvent)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        modifier = Modifier.padding(top = 6.dp, start = 4.dp),
        text = title.uppercase(),
        fontSize = 9.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        color = KFTheme.color.overlaySubtitleColor,
        maxLines = 1,
    )
}

/** Карточка буфера: тап вставляет и закрывает окно, долгое нажатие открывает меню. */
@Composable
private fun ClipboardCard(
    entry: ClipboardEntry,
    onEvent: (KeyboardEvent) -> Unit,
) {
    var isMenuOpen by remember(entry.id) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KeyShape)
            .background(KFTheme.color.keyButtonBackground)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onLongClick = { isMenuOpen = true },
                onClick = { onEvent(KeyboardEvent.OnClipboardPaste(entry)) },
            )
            .padding(horizontal = 10.dp, vertical = 9.dp)
            .padding(bottom = KeySupport)
    ) {
        Row {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                text = entry.text,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = KFTheme.color.keyTextColor,
                maxLines = CardMaxLines,
                overflow = TextOverflow.Ellipsis,
            )

            if (entry.isPinned) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_clipboard_pin),
                    contentDescription = null,
                    tint = KFTheme.color.overlayAccent,
                    modifier = Modifier
                        .align(Alignment.Top)
                        .padding(8.dp)
                        .size(16.dp)
                )
            }
        }

        ClipboardCardMenu(
            entry = entry,
            isOpen = isMenuOpen,
            onDismiss = { isMenuOpen = false },
            onEvent = onEvent,
        )
    }
}

@Composable
private fun EmptyClipboard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_clipboard),
            contentDescription = null,
            tint = KFTheme.color.overlaySubtitleColor.copy(alpha = 0.5f),
            modifier = Modifier.size(30.dp)
        )
        Text(
            text = stringResource(R.string.clipboard_empty),
            fontSize = 12.sp,
            color = KFTheme.color.overlaySubtitleColor,
        )
    }
}

private const val ClipboardColumns = 2

/** Сколько строк текста помещается в карточку — дальше многоточие. */
private const val CardMaxLines = 3
