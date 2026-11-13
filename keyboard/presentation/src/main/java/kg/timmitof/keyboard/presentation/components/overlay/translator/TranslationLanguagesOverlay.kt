package kg.timmitof.keyboard.presentation.components.overlay.translator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.R as UiR
import kg.timmitof.keyboard.presentation.components.keyboardClickable
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.presentation.components.overlay.OverlayHeader
import kg.timmitof.keyboard.presentation.screens.keyboard.rememberSlice
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
import kg.timmitof.keyboard.presentation.theme.KFTheme
import kg.timmitof.keyboard.translate.domain.model.ModelState
import kg.timmitof.keyboard.translate.domain.model.TranslationLanguage

/**
 * «Перевести на» (дизайн 8b): поиск, недавние языки чипами — их выбирают чаще всего, — и весь список,
 * скачанные сверху. Нескачанный язык можно скачать прямо из строки, не меняя пару.
 */
@Composable
internal fun ColumnScope.TranslationTargetOverlay(
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit,
) {
    val slice by state.rememberSlice(KeyboardState::targetSlice)

    OverlayHeader(
        title = stringResource(R.string.translator_target_title),
        onClose = { onEvent(KeyboardEvent.OnOverlayChange(null)) },
    )

    val picker = slice ?: return

    SearchEntry(onClick = { onEvent(KeyboardEvent.OnTranslatorSearchOpen) })

    if (picker.recent.isNotEmpty()) {
        RecentChips(
            recent = picker.recent,
            selectedCode = picker.selectedCode,
            onSelect = { onEvent(KeyboardEvent.OnTranslatorTargetSelect(it)) },
        )
    }

    LanguagesList(
        picker = picker,
        onSelect = { onEvent(KeyboardEvent.OnTranslatorTargetSelect(it)) },
        onDownload = { onEvent(KeyboardEvent.OnTranslatorLanguageDownload(it)) },
    )
}

/** «Перевести с»: включённые раскладки, которые знает переводчик; выбор переключает и раскладку. */
@Composable
internal fun ColumnScope.TranslationSourceOverlay(
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit,
) {
    val slice by state.rememberSlice(KeyboardState::sourceSlice)

    OverlayHeader(
        title = stringResource(R.string.translator_source_title),
        onClose = { onEvent(KeyboardEvent.OnOverlayChange(null)) },
    )

    val picker = slice ?: return

    // Дизайн 8b: «Определять автоматически» — исходный язык по тексту, раскладка остаётся.
    AutoSourceRow(
        isSelected = picker.isAuto,
        onClick = { onEvent(KeyboardEvent.OnTranslatorAutoSourceSelect) },
    )

    LanguagesList(
        picker = picker,
        onSelect = { onEvent(KeyboardEvent.OnTranslatorSourceSelect(it)) },
        onDownload = { onEvent(KeyboardEvent.OnTranslatorLanguageDownload(it)) },
    )
}

@Composable
private fun AutoSourceRow(
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(RowHeight)
            .clip(RowShape)
            .then(if (isSelected) Modifier.background(KFTheme.color.noticeBackground) else Modifier)
            .semantics { selected = isSelected }
            .keyboardClickable(onClick)
            .padding(start = 10.dp, end = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 30.dp)
                .clip(BadgeShape)
                .background(KFTheme.color.noticeBackground)
                .padding(horizontal = 7.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_translate),
                contentDescription = null,
                tint = KFTheme.color.noticeTextColor,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.translator_auto),
            fontSize = 14.sp,
            color = KFTheme.color.overlayTitleColor,
            maxLines = 1,
        )
        Text(
            text = stringResource(R.string.translator_auto_text),
            fontSize = 11.5.sp,
            color = KFTheme.color.overlaySubtitleColor,
            maxLines = 1,
        )
        if (isSelected) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_check_key),
                contentDescription = null,
                tint = KFTheme.color.noticeTextColor,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/** Не поле, а вход в поиск: лист закроется, и запрос наберут обычные клавиши. */
@Composable
private fun SearchEntry(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 8.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(KFTheme.color.overlayChipBackground)
            .keyboardClickable(onClick)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_search_key),
            contentDescription = null,
            tint = KFTheme.color.overlaySubtitleColor,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = stringResource(R.string.translator_search),
            fontSize = 13.sp,
            color = KFTheme.color.overlaySubtitleColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun RecentChips(
    recent: List<TranslationLanguage>,
    selectedCode: String,
    onSelect: (TranslationLanguage) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        items(items = recent, key = { it.code }) { language ->
            RecentChip(
                language = language,
                isSelected = language.code == selectedCode,
                onClick = { onSelect(language) },
            )
        }
    }
}

@Composable
private fun RecentChip(
    language: TranslationLanguage,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val content = if (isSelected) KFTheme.color.noticeTextColor else KFTheme.color.overlayTitleColor

    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (isSelected) KFTheme.color.noticeBackground else KFTheme.color.overlayChipBackground)
            .semantics { selected = isSelected }
            .keyboardClickable(onClick)
            .padding(horizontal = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isSelected) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_check_key),
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(13.dp),
            )
        }
        Text(
            text = language.name,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = content,
            maxLines = 1,
        )
    }
}

@Composable
private fun ColumnScope.LanguagesList(
    picker: LanguagesSlice,
    onSelect: (TranslationLanguage) -> Unit,
    onDownload: (TranslationLanguage) -> Unit,
) {
    val offline = stringResource(R.string.translator_offline)
    val download = stringResource(R.string.translator_download)

    LazyColumn(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 8.dp),
    ) {
        items(items = picker.languages, key = { it.code }) { language ->
            LanguageRow(
                language = language,
                isSelected = language.code == picker.selectedCode,
                state = picker.stateOf(language),
                offlineLabel = offline,
                downloadLabel = download,
                onClick = { onSelect(language) },
                onDownload = { onDownload(language) },
            )
        }
    }
}

@Composable
private fun LanguageRow(
    language: TranslationLanguage,
    isSelected: Boolean,
    state: ModelState,
    offlineLabel: String,
    downloadLabel: String,
    onClick: () -> Unit,
    onDownload: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(RowHeight)
            .clip(RowShape)
            .then(if (isSelected) Modifier.background(KFTheme.color.noticeBackground) else Modifier)
            .semantics { selected = isSelected }
            .keyboardClickable(onClick)
            .padding(start = 10.dp, end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CodeBadge(code = language.shortName)

        Text(
            modifier = Modifier.weight(1f),
            text = language.name,
            fontSize = 14.sp,
            color = KFTheme.color.overlayTitleColor,
            maxLines = 1,
        )

        if (isSelected) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_check_key),
                contentDescription = null,
                tint = KFTheme.color.noticeTextColor,
                modifier = Modifier.size(16.dp),
            )
        }

        // «офлайн» — скачан, «↓» — можно скачать, лоадер — уже качается.
        when (state) {
            ModelState.DOWNLOADED -> Text(
                modifier = Modifier.padding(end = 6.dp),
                text = offlineLabel,
                fontSize = 11.5.sp,
                color = KFTheme.color.overlaySubtitleColor,
                maxLines = 1,
            )

            ModelState.DOWNLOADING -> Box(modifier = Modifier.size(TrailingSize), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = KFTheme.color.overlayAccent,
                )
            }

            ModelState.MISSING -> Box(
                modifier = Modifier
                    .size(TrailingSize)
                    .clip(CircleShape)
                    .keyboardClickable(onDownload),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(UiR.drawable.ic_download),
                    contentDescription = downloadLabel,
                    tint = KFTheme.color.overlayAccent,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun CodeBadge(code: String) {
    Box(
        modifier = Modifier
            .widthIn(min = 30.dp)
            .clip(BadgeShape)
            .background(KFTheme.color.overlayChipBackground)
            .padding(horizontal = 5.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = code,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = KFTheme.color.overlayChipTextColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Immutable
private data class LanguagesSlice(
    val languages: List<TranslationLanguage>,
    val selectedCode: String,
    val downloadedCodes: Set<String>,
    val downloadingCodes: Set<String>,
    val recent: List<TranslationLanguage> = emptyList(),
    /** «Перевести с» в режиме «определять автоматически»: отмечена эта строка, а не язык. */
    val isAuto: Boolean = false,
) {
    fun stateOf(language: TranslationLanguage): ModelState = when (language.code) {
        in downloadedCodes -> ModelState.DOWNLOADED
        in downloadingCodes -> ModelState.DOWNLOADING
        else -> ModelState.MISSING
    }
}

/** Скачанные — сверху: их выбирают чаще всего, и они работают без сети. */
private fun KeyboardState.targetSlice(): LanguagesSlice? {
    val translator = translator ?: return null
    val models = translator.models

    return LanguagesSlice(
        languages = translator.languages.sortedByDescending { it.code in models.downloaded },
        selectedCode = translator.target.code,
        downloadedCodes = models.downloaded,
        downloadingCodes = models.downloading,
        recent = translator.recentTargets,
    )
}

private fun KeyboardState.sourceSlice(): LanguagesSlice? {
    val translator = translator ?: return null

    return LanguagesSlice(
        languages = translator.sourceLanguages,
        selectedCode = translator.source?.code?.takeUnless { translator.isAutoSource }.orEmpty(),
        isAuto = translator.isAutoSource,
        downloadedCodes = translator.models.downloaded,
        downloadingCodes = translator.models.downloading,
    )
}

private val RowHeight = 42.dp
private val TrailingSize = 32.dp
private val RowShape = RoundedCornerShape(10.dp)
private val BadgeShape = RoundedCornerShape(6.dp)
