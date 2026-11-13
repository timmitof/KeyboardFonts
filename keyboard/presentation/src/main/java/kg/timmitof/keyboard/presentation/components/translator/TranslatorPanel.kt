package kg.timmitof.keyboard.presentation.components.translator

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.R as UiR
import kg.timmitof.core.ui.plainClickable
import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.font.domain.model.KeyboardFont
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.presentation.components.FontsCarousel
import kg.timmitof.keyboard.presentation.components.InlineFieldText
import kg.timmitof.keyboard.presentation.components.InlineSearchBar
import kg.timmitof.keyboard.presentation.components.fontSample
import kg.timmitof.keyboard.presentation.components.fontScript
import kg.timmitof.keyboard.presentation.components.fontShortSample
import kg.timmitof.keyboard.presentation.components.orDefaultFor
import kg.timmitof.keyboard.presentation.components.supporting
import kg.timmitof.keyboard.presentation.components.topbar.SuggestionsRow
import kg.timmitof.keyboard.presentation.components.topbar.TopBarAnchorButton
import kg.timmitof.keyboard.presentation.components.topbar.TopBarPill
import kg.timmitof.keyboard.presentation.screens.keyboard.rememberSlice
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardOverlay
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
import kg.timmitof.keyboard.presentation.screens.keyboard.states.TranslatorNotice
import kg.timmitof.keyboard.presentation.theme.KFTheme
import kg.timmitof.keyboard.suggestion.domain.model.WordSuggestion
import kg.timmitof.keyboard.translate.domain.model.TranslationLanguage

/**
 * Панель перевода вместо верхней строки (дизайн 8a): пара языков и своё поле. Набранное здесь клавиатура
 * переводит и пишет в поле приложения; Enter отправляет перевод. Вместо поля — карточка, если переводить
 * пока нельзя (дизайн 8c): язык не скачан, качается, языки совпали.
 */
@Composable
internal fun TranslatorPanel(
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val slice by state.rememberSlice(KeyboardState::translatorPanelSlice)
    val panel = slice ?: return

    panel.search?.let { search ->
        LanguageSearch(search = search, onEvent = onEvent, modifier = modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, bottom = 2.dp),
        verticalArrangement = Arrangement.spacedBy(RowGap),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(RowHeight),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (panel.fonts != null && panel.isFontPickerOpen) {
                FontsRow(fonts = panel.fonts, onEvent = onEvent)
            } else {
                LanguagesRow(panel = panel, onEvent = onEvent)
            }
        }

        val notice = panel.notice
        if (notice == null) {
            PanelField(panel = panel, onEvent = onEvent)
        } else {
            NoticeCard(notice = notice, panel = panel, onEvent = onEvent)
        }
    }
}

/**
 * Поиск языка перевода вместо панели — как поиск эмодзи: запрос набирают клавиши, найденные — строкой ниже.
 * Тап выбирает язык и возвращает панель.
 */
@Composable
private fun LanguageSearch(
    search: LanguageSearchSlice,
    onEvent: (KeyboardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, bottom = 2.dp),
        verticalArrangement = Arrangement.spacedBy(RowGap),
    ) {
        InlineSearchBar(
            query = search.query,
            hint = stringResource(R.string.translator_search),
            selectionChars = search.selection,
            onClose = { onEvent(KeyboardEvent.OnTranslatorSearchClose) },
        )

        if (search.results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FieldHeight),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.translator_search_empty),
                    fontSize = 13.sp,
                    color = KFTheme.color.keySpecialTextColor,
                )
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FieldHeight),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items(items = search.results, key = { it.code }) { language ->
                    TopBarPill(
                        label = language.name,
                        isActive = language.code == search.targetCode,
                        onClick = { onEvent(KeyboardEvent.OnTranslatorTargetSelect(language)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.LanguagesRow(
    panel: TranslatorPanelSlice,
    onEvent: (KeyboardEvent) -> Unit,
) {
    // Исходный язык выбирается из включённых раскладок: выбор переключает и раскладку.
    TopBarPill(
        modifier = Modifier.widthIn(max = LanguageMaxWidth),
        label = if (panel.isAutoSource) stringResource(R.string.translator_auto_label, panel.sourceName) else panel.sourceName,
        hasDropdown = true,
        onClick = { onEvent(KeyboardEvent.OnOverlayChange(KeyboardOverlay.TRANSLATION_SOURCE)) },
    )

    if (panel.canSwap) {
        TopBarAnchorButton(
            iconRes = R.drawable.ic_swap,
            contentDescription = stringResource(R.string.translator_swap),
            isActive = false,
            onClick = { onEvent(KeyboardEvent.OnTranslatorSwap) },
        )
    } else {
        Text(text = "→", fontSize = 14.sp, color = KFTheme.color.keySpecialTextColor)
    }

    TopBarPill(
        modifier = Modifier.widthIn(max = LanguageMaxWidth),
        label = panel.targetName,
        hasDropdown = true,
        onClick = { onEvent(KeyboardEvent.OnOverlayChange(KeyboardOverlay.TRANSLATION_TARGET)) },
    )

    Spacer(modifier = Modifier.weight(1f))

    // «Aa» — шрифт перевода: открывает карусель, подсвечена, когда выбран не обычный шрифт.
    panel.fonts?.let { fonts ->
        TopBarPill(
            label = fonts.sample,
            isActive = fonts.isStyled,
            hasDropdown = true,
            onClick = { onEvent(KeyboardEvent.OnTranslatorFontsToggle) },
        )
    }

    TopBarAnchorButton(
        iconRes = R.drawable.ic_close,
        contentDescription = stringResource(R.string.translator_close),
        isActive = false,
        onClick = { onEvent(KeyboardEvent.OnTranslatorToggle) },
    )
}

@Composable
private fun RowScope.FontsRow(
    fonts: TranslatorFonts,
    onEvent: (KeyboardEvent) -> Unit,
) {
    FontsCarousel(
        fonts = fonts.available,
        selectedFontId = fonts.selected.id,
        sample = fonts.carouselSample,
        onFontSelect = { onEvent(KeyboardEvent.OnFontSelect(it)) },
        modifier = Modifier.weight(1f),
    )
    TopBarAnchorButton(
        iconRes = R.drawable.ic_close,
        contentDescription = stringResource(R.string.translator_fonts),
        isActive = true,
        onClick = { onEvent(KeyboardEvent.OnTranslatorFontsToggle) },
    )
}

@Composable
private fun PanelField(
    panel: TranslatorPanelSlice,
    onEvent: (KeyboardEvent) -> Unit,
) {
    val accent = KFTheme.color.keyAccentBackground

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(FieldHeight)
            .clip(FieldShape)
            .background(KFTheme.color.keyButtonBackground)
            .border(2.dp, accent, FieldShape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(FieldHeight)
                .padding(start = 14.dp, end = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InlineFieldText(
                modifier = Modifier.weight(1f),
                text = panel.text,
                hint = stringResource(R.string.translator_hint),
                selectionChars = panel.selection,
            )

            // Подсказки Т9 — по исходному языку, прямо в поле: отдельной строки у панели нет.
            if (panel.suggestions.isNotEmpty()) {
                SuggestionsRow(
                    suggestions = panel.suggestions,
                    onSelect = { onEvent(KeyboardEvent.OnSuggestionSelect(it)) },
                    maxSuggestions = FieldSuggestions,
                    modifier = Modifier.width(FieldSuggestionsWidth),
                )
            }

            if (panel.text.isNotEmpty()) {
                ClearButton(
                    contentDescription = stringResource(R.string.translator_clear),
                    onClick = { onEvent(KeyboardEvent.OnTranslatorClear) },
                )
            }
        }

        // Перевод в пути — тонкая полоска внизу поля, без спиннеров.
        if (panel.isTranslating) {
            LinearProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(3.dp),
                color = accent,
                trackColor = Color.Transparent,
            )
        }
    }
}

@Composable
private fun ClearButton(
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .plainClickable(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_close),
            contentDescription = contentDescription,
            tint = KFTheme.color.keySpecialTextColor,
            modifier = Modifier.size(15.dp),
        )
    }
}

/** Карточка вместо поля: что сделать, чтобы перевод заработал. Не ошибка, а предложение. */
@Composable
private fun NoticeCard(
    notice: TranslatorNotice,
    panel: TranslatorPanelSlice,
    onEvent: (KeyboardEvent) -> Unit,
) {
    val content = notice.content(panel)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = FieldHeight)
            .clip(FieldShape)
            .background(KFTheme.color.keyButtonBackground)
            .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(content.iconRes),
            contentDescription = null,
            tint = if (content.isHint) KFTheme.color.noticeTextColor else KFTheme.color.keyAccentBackground,
            modifier = Modifier.size(20.dp),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = content.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = KFTheme.color.keyLabelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = content.text,
                fontSize = 11.5.sp,
                color = KFTheme.color.keySpecialTextColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        when (val action = content.action) {
            null -> CircularProgressIndicator(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(20.dp),
                strokeWidth = 2.dp,
                color = KFTheme.color.keyAccentBackground,
            )

            else -> ActionPill(label = action.label, onClick = { onEvent(action.event) })
        }

        content.dismissEvent?.let { event ->
            ClearButton(
                contentDescription = stringResource(R.string.translator_detect_dismiss),
                onClick = { onEvent(event) },
            )
        }
    }
}

@Composable
private fun ActionPill(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(KFTheme.color.keyAccentBackground)
            .plainClickable(onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        text = label,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Medium,
        color = KFTheme.color.keyAccentTextColor,
        maxLines = 1,
    )
}

/**
 * Содержимое карточки; [action] null — идёт загрузка, вместо кнопки крутится лоадер.
 * [dismissEvent] — «×»: предложение можно отклонить (подсказку сменить раскладку).
 */
private class NoticeContent(
    @param:DrawableRes val iconRes: Int,
    val title: String,
    val text: String,
    val action: NoticeAction?,
    val isHint: Boolean = false,
    val dismissEvent: KeyboardEvent? = null,
)

private class NoticeAction(
    val label: String,
    val event: KeyboardEvent,
)

@Composable
private fun TranslatorNotice.content(panel: TranslatorPanelSlice): NoticeContent = when (this) {
    // Дизайн 8c: «Похоже, вы пишете на …» — с цитатой набранного и кнопкой той раскладки.
    TranslatorNotice.LANGUAGE_DETECTED -> NoticeContent(
        iconRes = R.drawable.ic_translate,
        title = stringResource(R.string.translator_detected_title),
        text = stringResource(R.string.translator_detected_quote, panel.text),
        action = panel.detectedLayout?.let { NoticeAction(it.displayName, KeyboardEvent.OnLanguageSelect(it)) },
        isHint = true,
        dismissEvent = KeyboardEvent.OnTranslatorDetectionDismiss,
    )

    TranslatorNotice.UNSUPPORTED_SOURCE -> NoticeContent(
        iconRes = R.drawable.ic_translate,
        title = stringResource(R.string.translator_unsupported_title, panel.sourceName),
        text = stringResource(R.string.translator_unsupported_text),
        action = NoticeAction(stringResource(R.string.translator_change), KeyboardEvent.OnOverlayChange(KeyboardOverlay.TRANSLATION_SOURCE)),
        isHint = true,
    )

    TranslatorNotice.SAME_LANGUAGES -> NoticeContent(
        iconRes = R.drawable.ic_translate,
        title = stringResource(R.string.translator_same_language_title),
        text = stringResource(R.string.translator_same_language_text),
        action = NoticeAction(stringResource(R.string.translator_change), KeyboardEvent.OnOverlayChange(KeyboardOverlay.TRANSLATION_TARGET)),
        isHint = true,
    )

    TranslatorNotice.MODEL_MISSING -> NoticeContent(
        iconRes = UiR.drawable.ic_download,
        title = stringResource(R.string.translator_model_missing_title, panel.missingNames),
        text = stringResource(R.string.translator_model_missing_text),
        action = NoticeAction(stringResource(R.string.translator_download), KeyboardEvent.OnTranslatorDownload),
    )

    TranslatorNotice.DOWNLOADING -> NoticeContent(
        iconRes = UiR.drawable.ic_download,
        title = stringResource(R.string.translator_downloading_title, panel.missingNames),
        text = stringResource(R.string.translator_downloading_text),
        action = null,
    )

    TranslatorNotice.DOWNLOAD_FAILED -> NoticeContent(
        iconRes = UiR.drawable.ic_download,
        title = stringResource(R.string.translator_download_failed_title, panel.missingNames),
        text = stringResource(R.string.translator_download_failed_text),
        action = NoticeAction(stringResource(R.string.translator_retry), KeyboardEvent.OnTranslatorDownload),
    )

    TranslatorNotice.TRANSLATION_FAILED -> NoticeContent(
        iconRes = R.drawable.ic_translate,
        title = stringResource(R.string.translator_failed_title),
        text = stringResource(R.string.translator_failed_text),
        action = NoticeAction(stringResource(R.string.translator_retry), KeyboardEvent.OnTranslatorRetry),
    )
}

/** Шрифты перевода — на алфавите целевого языка; null — стилизация в этом поле или настройках выключена. */
@Immutable
private data class TranslatorFonts(
    val available: List<KeyboardFont>,
    val selected: KeyboardFont,
    /** Образец «Aa» выбранным шрифтом — на кнопке. */
    val sample: String,
    val carouselSample: String,
) {
    val isStyled: Boolean get() = selected != KeyboardFont.Default
}

@Immutable
private data class LanguageSearchSlice(
    val query: String,
    val selection: Int,
    val results: List<TranslationLanguage>,
    val targetCode: String,
)

/** Всё, что читает панель; data class — срез сравнивается по значению. */
@Immutable
private data class TranslatorPanelSlice(
    /** На пилюле исходного языка; в режиме «автоматически» — «Авто · язык». */
    val sourceName: String,
    val isAutoSource: Boolean,
    /** Раскладка, на языке которой, похоже, набран текст, — для карточки «другой язык». */
    val detectedLayout: KeyboardLanguage?,
    val targetName: String,
    val text: String,
    val selection: Int,
    val suggestions: List<WordSuggestion>,
    val canSwap: Boolean,
    val isTranslating: Boolean,
    val notice: TranslatorNotice?,
    /** Нескачанные языки пары — для карточки «Скачайте …». */
    val missingNames: String,
    val fonts: TranslatorFonts?,
    val isFontPickerOpen: Boolean,
    /** Открыт поиск языка — вместо панели строка поиска. */
    val search: LanguageSearchSlice?,
)

private fun KeyboardState.translatorPanelSlice(): TranslatorPanelSlice? {
    val translator = translator ?: return null
    val target = translator.target
    val script = target.fontScript
    val selected = translationFont(target)

    return TranslatorPanelSlice(
        sourceName = translator.translationSource?.name ?: activeLanguage?.displayName.orEmpty(),
        isAutoSource = translator.isAutoSource,
        detectedLayout = translator.suggestedLayout,
        targetName = target.name,
        text = translator.editor.text,
        selection = translator.editor.selection,
        suggestions = if (hasSuggestions) suggestions else emptyList(),
        canSwap = translator.swapLayout != null,
        isTranslating = translator.isTranslating,
        notice = translator.notice,
        missingNames = translator.pairCodes
            .filterNot { it in translator.models.downloaded }
            .mapNotNull { code -> translator.languages.firstOrNull { it.code == code }?.name }
            .joinToString(),
        fonts = if (allowsTranslationFont) {
            TranslatorFonts(
                available = fonts.supporting(script),
                selected = selectedFont.orDefaultFor(script),
                sample = selected.apply(script.fontShortSample),
                carouselSample = script.fontSample,
            )
        } else {
            null
        },
        isFontPickerOpen = translator.isFontPickerOpen,
        search = translator.languageSearch?.let { query ->
            LanguageSearchSlice(
                query = query.text,
                selection = query.selection,
                results = translator.searchResults,
                targetCode = target.code,
            )
        },
    )
}

private val RowHeight = 40.dp
private val RowGap = 6.dp
private val FieldHeight = 44.dp
private val FieldShape = RoundedCornerShape(12.dp)
private val LanguageMaxWidth = 128.dp
private val FieldSuggestionsWidth = 168.dp
private const val FieldSuggestions = 2
