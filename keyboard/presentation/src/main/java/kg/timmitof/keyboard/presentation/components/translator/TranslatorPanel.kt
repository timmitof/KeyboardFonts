package kg.timmitof.keyboard.presentation.components.translator

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.plainClickable
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.presentation.components.InlineFieldText
import kg.timmitof.keyboard.presentation.components.fontScript
import kg.timmitof.keyboard.presentation.components.fontShortSample
import kg.timmitof.keyboard.presentation.components.topbar.TopBarAnchorButton
import kg.timmitof.keyboard.presentation.components.topbar.TopBarPill
import kg.timmitof.keyboard.presentation.screens.keyboard.rememberSlice
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardOverlay
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
import kg.timmitof.keyboard.presentation.screens.keyboard.states.TranslatorStatus
import kg.timmitof.keyboard.presentation.theme.KFTheme

/**
 * Панель перевода над верхней строкой: пара языков и своё поле. Набранное здесь клавиатура переводит
 * и пишет в поле приложения; Enter отправляет перевод.
 */
@Composable
internal fun TranslatorPanel(
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val slice by state.rememberSlice(KeyboardState::translatorPanelSlice)
    val panel = slice ?: return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LanguagesRow(panel = panel, onEvent = onEvent)
        PanelField(panel = panel, onEvent = onEvent)
    }
}

@Composable
private fun LanguagesRow(
    panel: TranslatorPanelSlice,
    onEvent: (KeyboardEvent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(RowHeight),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Исходный язык не выбирается: он всегда язык раскладки, меняется вместе с ней.
        Text(
            modifier = Modifier
                .widthIn(max = LanguageMaxWidth)
                .padding(start = 8.dp, end = 2.dp),
            text = panel.sourceName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = KFTheme.color.keySpecialTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
            onClick = { onEvent(KeyboardEvent.OnOverlayChange(KeyboardOverlay.TRANSLATION_LANGUAGES)) },
        )

        Spacer(modifier = Modifier.weight(1f))

        if (panel.fontSample != null) {
            TopBarPill(
                label = panel.fontSample,
                isActive = panel.isStyled,
                onClick = { onEvent(KeyboardEvent.OnTranslatorStyledToggle) },
            )
        }

        TopBarAnchorButton(
            iconRes = R.drawable.ic_close,
            contentDescription = stringResource(R.string.translator_close),
            isActive = false,
            onClick = { onEvent(KeyboardEvent.OnTranslatorToggle) },
        )
    }
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
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InlineFieldText(
                modifier = Modifier.weight(1f),
                text = panel.text,
                hint = panel.message.text(panel),
                selectionChars = panel.selection,
            )

            panel.action?.let { action ->
                FieldAction(
                    label = stringResource(action.labelRes),
                    onClick = { onEvent(KeyboardEvent.OnTranslatorDownload) },
                )
            }
        }

        // Перевод в пути — тонкая полоска внизу поля, без спиннеров.
        if (panel.isTranslating) {
            LinearProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.dp),
                color = accent,
                trackColor = Color.Transparent,
            )
        }
    }
}

@Composable
private fun FieldAction(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        modifier = Modifier
            .clip(RoundedCornerShape(ActionHeight / 2))
            .background(KFTheme.color.keyAccentBackground)
            .plainClickable(onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        text = label,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Medium,
        color = KFTheme.color.keyAccentTextColor,
        maxLines = 1,
    )
}

@Composable
private fun PanelMessage.text(panel: TranslatorPanelSlice): String = when (this) {
    PanelMessage.HINT -> stringResource(R.string.translator_hint)
    PanelMessage.UNSUPPORTED -> stringResource(R.string.translator_unsupported, panel.sourceName)
    PanelMessage.SAME_LANGUAGE -> stringResource(R.string.translator_same_language)
    PanelMessage.MODEL_MISSING -> stringResource(R.string.translator_model_missing, panel.targetName)
    PanelMessage.DOWNLOADING -> stringResource(R.string.translator_downloading, panel.targetName)
    PanelMessage.DOWNLOAD_FAILED -> stringResource(R.string.translator_download_failed)
    PanelMessage.FAILED -> stringResource(R.string.translator_failed)
}

/** Что панель пишет в пустом поле: подсказку или почему перевода нет. */
private enum class PanelMessage { HINT, UNSUPPORTED, SAME_LANGUAGE, MODEL_MISSING, DOWNLOADING, DOWNLOAD_FAILED, FAILED }

/** Кнопка справа в поле — предложение, а не ошибка: скачать язык, чтобы работало офлайн. */
private enum class FieldActionKind(@param:StringRes val labelRes: Int) {
    DOWNLOAD(R.string.translator_download),
    RETRY(R.string.translator_retry),
}

/** Всё, что читает панель; data class — срез сравнивается по значению. */
@Immutable
private data class TranslatorPanelSlice(
    val sourceName: String,
    val targetName: String,
    val text: String,
    val selection: Int,
    val canSwap: Boolean,
    val isTranslating: Boolean,
    val isStyled: Boolean,
    /** Образец «Aa» выбранным шрифтом; null — шрифты в этом поле выключены. */
    val fontSample: String?,
    val message: PanelMessage,
    val action: FieldActionKind?,
)

private fun KeyboardState.translatorPanelSlice(): TranslatorPanelSlice? {
    val translator = translator ?: return null
    val target = translator.target

    return TranslatorPanelSlice(
        sourceName = translator.source?.name ?: activeLanguage?.displayName.orEmpty(),
        targetName = target.name,
        text = translator.editor.text,
        selection = translator.editor.selection,
        canSwap = translator.swapLayout != null,
        isTranslating = translator.isTranslating,
        isStyled = translator.isStyled,
        fontSample = if (allowsTranslationFont) {
            translationFont(target, isStyled = true).apply(target.fontScript.fontShortSample)
        } else {
            null
        },
        message = when {
            translator.source == null -> PanelMessage.UNSUPPORTED
            !translator.canTranslate -> PanelMessage.SAME_LANGUAGE
            else -> when (translator.status) {
                TranslatorStatus.READY -> PanelMessage.HINT
                TranslatorStatus.MODEL_MISSING -> PanelMessage.MODEL_MISSING
                TranslatorStatus.DOWNLOADING -> PanelMessage.DOWNLOADING
                TranslatorStatus.DOWNLOAD_FAILED -> PanelMessage.DOWNLOAD_FAILED
                TranslatorStatus.FAILED -> PanelMessage.FAILED
            }
        },
        action = when (translator.status) {
            TranslatorStatus.MODEL_MISSING -> FieldActionKind.DOWNLOAD
            TranslatorStatus.DOWNLOAD_FAILED -> FieldActionKind.RETRY
            else -> null
        },
    )
}

private val RowHeight = 40.dp
private val FieldHeight = 44.dp
private val ActionHeight = 30.dp
private val FieldShape = RoundedCornerShape(12.dp)
private val LanguageMaxWidth = 120.dp
