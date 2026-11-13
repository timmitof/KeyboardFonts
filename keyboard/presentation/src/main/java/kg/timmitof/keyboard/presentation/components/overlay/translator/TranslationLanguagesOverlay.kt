package kg.timmitof.keyboard.presentation.components.overlay.translator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.plainClickable
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.presentation.components.overlay.OverlayHeader
import kg.timmitof.keyboard.presentation.screens.keyboard.rememberSlice
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
import kg.timmitof.keyboard.presentation.theme.KFTheme
import kg.timmitof.keyboard.translate.domain.model.TranslationLanguage

/** Выбор языка перевода поверх клавиш: скачанные — сверху, они работают без сети. */
@Composable
internal fun ColumnScope.TranslationLanguagesOverlay(
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit,
) {
    val slice by state.rememberSlice(KeyboardState::languagesSlice)

    OverlayHeader(
        title = stringResource(R.string.translator_target_title),
        onClose = { onEvent(KeyboardEvent.OnOverlayChange(null)) },
    )

    val picker = slice ?: return
    val offline = stringResource(R.string.translator_offline)

    LazyColumn(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 8.dp),
    ) {
        items(items = picker.languages, key = { it.code }) { language ->
            LanguageRow(
                language = language,
                isSelected = language.code == picker.selectedCode,
                offlineLabel = offline.takeIf { language.code in picker.downloadedCodes },
                onClick = { onEvent(KeyboardEvent.OnTranslatorTargetSelect(language)) },
            )
        }
    }
}

@Composable
private fun LanguageRow(
    language: TranslationLanguage,
    isSelected: Boolean,
    offlineLabel: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(RowHeight)
            .clip(RowShape)
            .then(if (isSelected) Modifier.background(KFTheme.color.noticeBackground) else Modifier)
            .plainClickable(onClick)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 30.dp)
                .clip(BadgeShape)
                .background(KFTheme.color.overlayChipBackground)
                .padding(horizontal = 5.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = language.shortName,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = KFTheme.color.overlayChipTextColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }

        Text(
            modifier = Modifier.weight(1f),
            text = language.name,
            fontSize = 14.sp,
            color = KFTheme.color.overlayTitleColor,
            maxLines = 1,
        )

        offlineLabel?.let {
            Text(
                text = it,
                fontSize = 11.5.sp,
                color = KFTheme.color.overlaySubtitleColor,
                maxLines = 1,
            )
        }

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

@Immutable
private data class LanguagesSlice(
    val languages: List<TranslationLanguage>,
    val selectedCode: String,
    val downloadedCodes: Set<String>,
)

/** Скачанные — сверху: их выбирают чаще всего, и они работают без сети. */
private fun KeyboardState.languagesSlice(): LanguagesSlice? {
    val translator = translator ?: return null
    val downloaded = translator.models.downloaded

    return LanguagesSlice(
        languages = translator.languages.sortedByDescending { it.code in downloaded },
        selectedCode = translator.target.code,
        downloadedCodes = downloaded,
    )
}

private val RowHeight = 42.dp
private val RowShape = RoundedCornerShape(10.dp)
private val BadgeShape = RoundedCornerShape(6.dp)
