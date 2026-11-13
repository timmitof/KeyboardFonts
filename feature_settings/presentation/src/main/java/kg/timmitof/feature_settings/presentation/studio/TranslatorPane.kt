package kg.timmitof.feature_settings.presentation.studio

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kg.timmitof.core.ui.R as UiR
import kg.timmitof.core.ui.components.settings.SettingsCheckList
import kg.timmitof.core.ui.components.settings.SettingsListTallRowHeight
import kg.timmitof.core.ui.components.settings.SettingsPickSection
import kg.timmitof.core.ui.components.settings.SettingsPickSheet
import kg.timmitof.core.ui.components.settings.SettingsPillButton
import kg.timmitof.core.ui.components.settings.SettingsSection
import kg.timmitof.core.ui.components.settings.SettingsSectionHeader
import kg.timmitof.core.ui.permissions.rememberNotificationPermissionRequest
import kg.timmitof.feature_settings.domain.model.TranslatorLanguageItem
import kg.timmitof.feature_settings.domain.model.TranslatorOverview
import kg.timmitof.feature_settings.presentation.R
import kg.timmitof.feature_settings.presentation.components.ConnectedMark
import kg.timmitof.feature_settings.presentation.components.LanguageRowContent
import kg.timmitof.feature_settings.presentation.components.RowIconButton
import kg.timmitof.feature_settings.presentation.components.RowProgress
import kg.timmitof.keyboard.domain.model.FeatureAccess
import kg.timmitof.keyboard.domain.model.KeyboardFeature
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kg.timmitof.keyboard.translate.domain.model.ModelState

/**
 * Переводчик: выключатель фичи ([KeyboardFeature.TRANSLATOR]; закрытая платная видна, но не включается),
 * языки пользователя с их состоянием и «Добавить язык перевода» — шторка с каталогом, как у раскладок.
 */
@Composable
internal fun TranslatorPane(
    settings: KeyboardSettings,
    access: FeatureAccess,
    translator: TranslatorOverview,
    onToggle: (KeyboardToggle, Boolean) -> Unit,
    onDownloadLanguage: (String) -> Unit,
    onDeleteLanguage: (String) -> Unit,
) {
    val feature = KeyboardFeature.TRANSLATOR
    var isPickerOpen by rememberSaveable { mutableStateOf(false) }
    // Удаление — через подтверждение: скачивать ~30 МБ заново из-за случайного нажатия обидно.
    var deleteCode by rememberSaveable { mutableStateOf<String?>(null) }
    val onAskDelete = remember { { code: String -> deleteCode = code } }
    // Разрешение на уведомления — при открытии шторки: дальше пользователь качает и видит прогресс.
    val notificationPermission = rememberNotificationPermissionRequest()

    val toggleTitle = stringResource(R.string.translator_toggle_title)
    val toggleDescription = stringResource(R.string.translator_toggle_description)
    val labels = TranslatorLabels(
        builtIn = stringResource(R.string.translator_state_built_in),
        downloaded = stringResource(R.string.translator_state_downloaded),
        downloading = stringResource(R.string.translator_state_downloading),
        missing = stringResource(R.string.translator_state_missing),
        download = stringResource(R.string.translator_download),
        delete = stringResource(R.string.translator_delete),
    )

    SettingsSection {
        toggle(
            title = toggleTitle,
            description = toggleDescription,
            checked = settings[feature.toggle],
            isEnabled = access.isUnlocked(feature),
            onCheckedChange = { onToggle(feature.toggle, it) },
        )
    }

    if (!settings.isActive(feature, access)) return

    if (translator.languages.isNotEmpty()) {
        Column {
            SettingsSectionHeader(title = stringResource(R.string.translator_languages_header))
            SettingsCheckList(
                items = translator.languages,
                key = { it.language.code },
                rowHeight = SettingsListTallRowHeight,
            ) { item ->
                TranslatorLanguageRow(
                    item = item,
                    labels = labels,
                    onDownload = onDownloadLanguage,
                    onDelete = onAskDelete,
                )
            }
        }
    }

    SettingsPillButton(
        modifier = Modifier.fillMaxWidth(),
        label = stringResource(R.string.translator_add),
        icon = painterResource(UiR.drawable.ic_plus),
        onClick = { notificationPermission.runWithPermission { isPickerOpen = true } },
    )

    if (isPickerOpen) {
        TranslatorPickSheet(
            catalog = translator.catalog,
            labels = labels,
            onDownload = onDownloadLanguage,
            onDelete = onAskDelete,
            onDismiss = { isPickerOpen = false },
        )
    }

    // Язык ищем в каталоге: строка могла уже пропасть из списка, а название нужно для вопроса.
    val pending = deleteCode?.let { code -> translator.catalog.firstOrNull { it.language.code == code } }
    if (pending != null) {
        DeleteLanguageDialog(
            name = pending.language.name,
            onConfirm = {
                deleteCode = null
                onDeleteLanguage(pending.language.code)
            },
            onDismiss = { deleteCode = null },
        )
    }
}

@Composable
private fun DeleteLanguageDialog(
    name: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.translator_delete_question, name)) },
        text = { Text(text = stringResource(R.string.translator_delete_text)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.translator_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.translator_delete_cancel))
            }
        },
    )
}

/** Скачанные — сверху с галочкой; остальные качаются нажатием, качающиеся крутят лоадер. Шторка не закрывается. */
@Composable
private fun TranslatorPickSheet(
    catalog: List<TranslatorLanguageItem>,
    labels: TranslatorLabels,
    onDownload: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val downloadedTitle = stringResource(R.string.translator_downloaded_header)
    val catalogTitle = stringResource(R.string.translator_catalog_header)

    val sections = remember(catalog, downloadedTitle, catalogTitle, onDownload) {
        val (downloaded, rest) = catalog.partition { it.state == ModelState.DOWNLOADED }
        listOf(
            SettingsPickSection(title = downloadedTitle, items = downloaded),
            SettingsPickSection(
                title = catalogTitle,
                items = rest,
                onClick = { item -> if (item.state == ModelState.MISSING) onDownload(item.language.code) },
            ),
        )
    }

    SettingsPickSheet(
        title = stringResource(R.string.translator_add_title),
        searchPlaceholder = stringResource(R.string.translator_search),
        sections = sections,
        key = { it.language.code },
        matches = { item, query -> item.matches(query) },
        onDismiss = onDismiss,
    ) { item ->
        TranslatorLanguageRow(item = item, labels = labels, onDownload = onDownload, onDelete = onDelete)
    }
}

@Composable
private fun RowScope.TranslatorLanguageRow(
    item: TranslatorLanguageItem,
    labels: TranslatorLabels,
    onDownload: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    LanguageRowContent(
        badge = item.language.shortName,
        title = item.language.name,
        description = labels.describe(item),
    )
    when (item.state) {
        // Встроенный язык удалить нельзя — у него просто галочка.
        ModelState.DOWNLOADED -> if (item.isBuiltIn) {
            ConnectedMark()
        } else {
            RowIconButton(
                iconRes = UiR.drawable.ic_trash,
                contentDescription = labels.delete,
                onClick = { onDelete(item.language.code) },
            )
        }

        ModelState.DOWNLOADING -> RowProgress()
        ModelState.MISSING -> RowIconButton(
            iconRes = UiR.drawable.ic_download,
            contentDescription = labels.download,
            onClick = { onDownload(item.language.code) },
        )
    }
}

/** Подписи читаются заранее: строки списков рисуются часто, а ресурсы им читать незачем. */
@Immutable
private class TranslatorLabels(
    val builtIn: String,
    val downloaded: String,
    val downloading: String,
    val missing: String,
    val download: String,
    val delete: String,
) {
    fun describe(item: TranslatorLanguageItem): String = when {
        item.isBuiltIn -> builtIn
        item.state == ModelState.DOWNLOADED -> downloaded
        item.state == ModelState.DOWNLOADING -> downloading
        else -> missing
    }
}

private fun TranslatorLanguageItem.matches(query: String): Boolean =
    language.name.contains(query, ignoreCase = true) || language.code.contains(query, ignoreCase = true)
