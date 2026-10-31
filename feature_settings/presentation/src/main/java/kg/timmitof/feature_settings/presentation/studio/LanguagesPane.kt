package kg.timmitof.feature_settings.presentation.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.resources.compose.stringResource
import kg.timmitof.core.resources.MR
import kg.timmitof.core.resources.languages_add
import kg.timmitof.core.resources.languages_add_action
import kg.timmitof.core.resources.languages_add_title
import kg.timmitof.core.resources.languages_catalog_header
import kg.timmitof.core.resources.languages_connected_header
import kg.timmitof.core.resources.languages_current
import kg.timmitof.core.resources.languages_header
import kg.timmitof.core.resources.languages_remove
import kg.timmitof.core.resources.languages_reorder_hint
import kg.timmitof.core.resources.languages_search
import kg.timmitof.core.resources.languages_with_suggestions
import kg.timmitof.core.resources.languages_without_suggestions
import kg.timmitof.core.ui.R as UiR
import kg.timmitof.core.ui.components.settings.SettingsBadge
import kg.timmitof.core.ui.components.settings.SettingsListTallRowHeight
import kg.timmitof.core.ui.components.settings.SettingsPickSection
import kg.timmitof.core.ui.components.settings.SettingsPickSheet
import kg.timmitof.core.ui.components.settings.SettingsPillButton
import kg.timmitof.core.ui.components.settings.SettingsReorderList
import kg.timmitof.core.ui.components.settings.SettingsSectionFooter
import kg.timmitof.core.ui.components.settings.SettingsSectionHeader
import kg.timmitof.core.ui.theme.appColors
import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.domain.model.KeyboardLanguages

/** Строка списка: подпись под названием считается заранее, чтобы строка не читала ресурсы. */
@Immutable
private data class LanguageItem(
    val language: KeyboardLanguage,
    val description: String,
)

/**
 * Включённые языки в порядке переключения: перестановка за ручку, крестик убирает язык,
 * «Добавить язык» открывает шторку с каталогом. Наружу уходит только новый список кодов.
 */
@Composable
internal fun LanguagesPane(
    languages: KeyboardLanguages?,
    onEnabledLanguages: (List<String>) -> Unit,
) {
    languages ?: return

    var isPickerOpen by rememberSaveable { mutableStateOf(false) }

    val currentLabel = stringResource(MR.strings.languages_current)
    val withSuggestions = stringResource(MR.strings.languages_with_suggestions)
    val withoutSuggestions = stringResource(MR.strings.languages_without_suggestions)

    val describe = remember(withSuggestions, withoutSuggestions) {
        { language: KeyboardLanguage -> if (language.hasDictionary) withSuggestions else withoutSuggestions }
    }
    val enabled = remember(languages.enabled, languages.selected, describe, currentLabel) {
        languages.enabled.map { language ->
            LanguageItem(
                language = language,
                description = if (language == languages.selected) currentLabel else describe(language),
            )
        }
    }
    val enabledCodes = remember(languages.enabled) { languages.enabled.map(KeyboardLanguage::code) }
    // Последний язык не убираем: без языка клавиатуре нечего показывать.
    val canRemove = enabled.size > 1

    val currentCodes by rememberUpdatedState(enabledCodes)
    val currentOnEnabled by rememberUpdatedState(onEnabledLanguages)
    val onRemove = remember { { code: String -> currentOnEnabled(currentCodes - code) } }
    val onAdd = remember { { language: KeyboardLanguage -> currentOnEnabled(currentCodes + language.code) } }

    Column {
        SettingsSectionHeader(title = stringResource(MR.strings.languages_header))
        SettingsReorderList(
            items = enabled,
            key = { it.language.code },
            onReorder = { items -> onEnabledLanguages(items.map { it.language.code }) },
            rowHeight = SettingsListTallRowHeight,
        ) { item ->
            LanguageRowContent(language = item.language, description = item.description)
            if (canRemove) RemoveButton(onClick = { onRemove(item.language.code) })
        }
        SettingsSectionFooter(text = stringResource(MR.strings.languages_reorder_hint))
    }

    SettingsPillButton(
        modifier = Modifier.fillMaxWidth(),
        label = stringResource(MR.strings.languages_add),
        icon = painterResource(UiR.drawable.ic_plus),
        onClick = { isPickerOpen = true },
    )

    if (isPickerOpen) {
        LanguagePickSheet(
            languages = languages,
            describe = describe,
            onAdd = onAdd,
            onDismiss = { isPickerOpen = false },
        )
    }
}

/** Подключённые отмечены галочкой, остальные добавляются нажатием; шторка остаётся открытой. */
@Composable
private fun LanguagePickSheet(
    languages: KeyboardLanguages,
    describe: (KeyboardLanguage) -> String,
    onAdd: (KeyboardLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    val connectedTitle = stringResource(MR.strings.languages_connected_header)
    val catalogTitle = stringResource(MR.strings.languages_catalog_header)
    val addLabel = stringResource(MR.strings.languages_add_action)

    val sections = remember(languages, connectedTitle, catalogTitle, onAdd) {
        listOf(
            SettingsPickSection(title = connectedTitle, items = languages.enabled),
            SettingsPickSection(
                title = catalogTitle,
                items = languages.catalog.filter { it !in languages.enabled },
                onClick = onAdd,
            ),
        )
    }

    SettingsPickSheet(
        title = stringResource(MR.strings.languages_add_title),
        searchPlaceholder = stringResource(MR.strings.languages_search),
        sections = sections,
        key = KeyboardLanguage::code,
        matches = { language, query -> language.matches(query) },
        onDismiss = onDismiss,
    ) { language ->
        LanguageRowContent(language = language, description = describe(language))
        if (language in languages.enabled) {
            ConnectedMark()
        } else {
            SettingsPillButton(label = addLabel, isCompact = true, onClick = { onAdd(language) })
        }
    }
}

private fun KeyboardLanguage.matches(query: String): Boolean =
    displayName.contains(query, ignoreCase = true) ||
        shortName.contains(query, ignoreCase = true) ||
        code.contains(query, ignoreCase = true)

@Composable
private fun RowScope.LanguageRowContent(language: KeyboardLanguage, description: String) {
    SettingsBadge(text = language.shortName)
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = language.displayName,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
        )
        Text(
            text = description,
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
        )
    }
}

@Composable
private fun RemoveButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(UiR.drawable.ic_close),
            contentDescription = stringResource(MR.strings.languages_remove),
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun ConnectedMark() {
    val tones = MaterialTheme.appColors.success

    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(tones.container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(UiR.drawable.ic_check),
            contentDescription = null,
            tint = tones.solid,
            modifier = Modifier.size(13.dp),
        )
    }
}
