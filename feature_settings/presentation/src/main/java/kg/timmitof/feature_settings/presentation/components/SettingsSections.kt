package kg.timmitof.feature_settings.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kg.timmitof.core.ui.components.settings.SettingsSection
import kg.timmitof.feature_settings.presentation.R
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle

/**
 * Секции экрана настроек.
 *
 * Ресурсы читаются до DSL: сборщик строк намеренно не композабельный, чтобы
 * список строк собирался одинаково при любой рекомпозиции.
 */

/** Клавиатура Fonts: подключение и проверка. */
@Composable
internal fun AppSection(
    isKeyboardReady: Boolean,
    onCheckKeyboard: () -> Unit,
) {
    val sectionTitle = stringResource(R.string.settings_section_app)
    val icon = painterResource(R.drawable.ic_settings_check)
    val title = stringResource(R.string.settings_check_title)
    val description = stringResource(R.string.settings_check_description)
    val status = stringResource(
        if (isKeyboardReady) R.string.settings_check_ready else R.string.settings_check_not_ready
    )

    SettingsSection(title = sectionTitle) {
        navigation(
            title = title,
            description = description,
            icon = icon,
            value = status,
            onClick = onCheckKeyboard,
        )
    }
}

/** Ввод текста: Т9 и всё, что от него зависит. */
@Composable
internal fun TextInputSection(
    settings: KeyboardSettings,
    onToggle: (KeyboardToggle, Boolean) -> Unit,
) {
    val sectionTitle = stringResource(R.string.settings_section_input)
    val icon = painterResource(R.drawable.ic_settings_input)

    val isSuggestionsOn = settings[KeyboardToggle.SUGGESTIONS]

    val suggestionsTitle = stringResource(R.string.settings_suggestions_title)
    val suggestionsDescription = stringResource(
        if (isSuggestionsOn) {
            R.string.settings_suggestions_description
        } else {
            R.string.settings_suggestions_description_off
        }
    )
    val autoCorrectTitle = stringResource(R.string.settings_autocorrect_title)
    val autoCorrectDescription = stringResource(R.string.settings_autocorrect_description)
    val spaceCommitTitle = stringResource(R.string.settings_space_commit_title)
    val spaceCommitDescription = stringResource(R.string.settings_space_commit_description)
    val nextWordTitle = stringResource(R.string.settings_next_word_title)
    val learnTitle = stringResource(R.string.settings_learn_title)
    val learnDescription = stringResource(R.string.settings_learn_description)

    SettingsSection(title = sectionTitle) {
        toggle(
            title = suggestionsTitle,
            description = suggestionsDescription,
            icon = icon,
            checked = isSuggestionsOn,
            onCheckedChange = { onToggle(KeyboardToggle.SUGGESTIONS, it) },
        )
        toggle(
            title = autoCorrectTitle,
            description = autoCorrectDescription,
            isNested = true,
            isEnabled = isSuggestionsOn,
            checked = settings[KeyboardToggle.AUTO_CORRECT],
            onCheckedChange = { onToggle(KeyboardToggle.AUTO_CORRECT, it) },
        )
        toggle(
            title = spaceCommitTitle,
            description = spaceCommitDescription,
            isNested = true,
            isEnabled = isSuggestionsOn,
            checked = settings[KeyboardToggle.SPACE_COMMITS],
            onCheckedChange = { onToggle(KeyboardToggle.SPACE_COMMITS, it) },
        )
        toggle(
            title = nextWordTitle,
            isNested = true,
            isEnabled = isSuggestionsOn,
            checked = settings[KeyboardToggle.NEXT_WORD_PREDICTION],
            onCheckedChange = { onToggle(KeyboardToggle.NEXT_WORD_PREDICTION, it) },
        )
        toggle(
            title = learnTitle,
            description = learnDescription,
            isNested = true,
            isEnabled = isSuggestionsOn,
            checked = settings[KeyboardToggle.LEARN_FROM_INPUT],
            onCheckedChange = { onToggle(KeyboardToggle.LEARN_FROM_INPUT, it) },
        )
    }
}

/** Шрифты: панель над клавишами и её содержимое. */
@Composable
internal fun FontsSection(
    settings: KeyboardSettings,
    fontsTotal: Int,
    onToggle: (KeyboardToggle, Boolean) -> Unit,
) {
    val sectionTitle = stringResource(R.string.settings_section_fonts)
    val icon = painterResource(R.drawable.ic_settings_fonts)

    val isFontsOn = settings[KeyboardToggle.STYLED_FONTS]

    val fontsTitle = stringResource(R.string.settings_styled_fonts_title)
    val fontsDescription = stringResource(R.string.settings_styled_fonts_description)
    val fontSetTitle = stringResource(R.string.settings_font_set_title)
    val fontSetDescription = stringResource(R.string.settings_font_set_description)
    val fontSetValue = stringResource(R.string.settings_font_set_value, fontsTotal)
    val rememberFontTitle = stringResource(R.string.settings_remember_font_title)

    SettingsSection(title = sectionTitle) {
        toggle(
            title = fontsTitle,
            description = fontsDescription,
            icon = icon,
            checked = isFontsOn,
            onCheckedChange = { onToggle(KeyboardToggle.STYLED_FONTS, it) },
        )
        navigation(
            title = fontSetTitle,
            description = fontSetDescription,
            value = fontSetValue,
            isNested = true,
            // Подэкран выбора шрифтов ещё не сделан — строка показывает состав панели
            isEnabled = false,
            onClick = { },
        )
        toggle(
            title = rememberFontTitle,
            isNested = true,
            isEnabled = isFontsOn,
            checked = settings[KeyboardToggle.REMEMBER_FONT],
            onCheckedChange = { onToggle(KeyboardToggle.REMEMBER_FONT, it) },
        )
    }
}

/** Клавиатура: раскладки, размер, цифровой ряд и тема. */
@Composable
internal fun KeyboardSection(
    settings: KeyboardSettings,
    languages: String,
    onToggle: (KeyboardToggle, Boolean) -> Unit,
    onTheme: (KeyboardThemeMode) -> Unit,
) {
    val sectionTitle = stringResource(R.string.settings_section_keyboard)
    val icon = painterResource(R.drawable.ic_settings_keyboard)

    val languagesTitle = stringResource(R.string.settings_languages_title)
    val sizeTitle = stringResource(R.string.settings_size_title)
    val sizeDescription = stringResource(R.string.settings_size_description)
    val sizeValue = stringResource(R.string.settings_size_value)
    val digitsRowTitle = stringResource(R.string.settings_digits_row_title)
    val digitsRowDescription = stringResource(R.string.settings_digits_row_description)
    val themeTitle = stringResource(R.string.settings_theme_title)
    val themeOptions = listOf(
        stringResource(R.string.settings_theme_auto),
        stringResource(R.string.settings_theme_light),
        stringResource(R.string.settings_theme_dark),
    )

    SettingsSection(title = sectionTitle) {
        navigation(
            title = languagesTitle,
            icon = icon,
            value = languages,
            // Экран «Языки и раскладки» — следующий шаг, пока показываем текущий набор
            isEnabled = false,
            onClick = { },
        )
        navigation(
            title = sizeTitle,
            description = sizeDescription,
            value = sizeValue,
            isEnabled = false,
            onClick = { },
        )
        toggle(
            title = digitsRowTitle,
            description = digitsRowDescription,
            checked = settings[KeyboardToggle.DIGITS_ROW],
            onCheckedChange = { onToggle(KeyboardToggle.DIGITS_ROW, it) },
        )
        segmented(
            title = themeTitle,
            options = themeOptions,
            selectedIndex = settings.theme.ordinal,
            onSelect = { index -> onTheme(KeyboardThemeMode.entries[index]) },
        )
    }
}

/** Отклик: как клавиатура отзывается на нажатие. */
@Composable
internal fun FeedbackSection(
    settings: KeyboardSettings,
    onToggle: (KeyboardToggle, Boolean) -> Unit,
) {
    val sectionTitle = stringResource(R.string.settings_section_feedback)
    val icon = painterResource(R.drawable.ic_settings_feedback)

    val vibrationTitle = stringResource(R.string.settings_vibration_title)
    val soundTitle = stringResource(R.string.settings_sound_title)
    val keyPreviewTitle = stringResource(R.string.settings_key_preview_title)

    SettingsSection(title = sectionTitle) {
        toggle(
            title = vibrationTitle,
            icon = icon,
            checked = settings[KeyboardToggle.VIBRATION],
            onCheckedChange = { onToggle(KeyboardToggle.VIBRATION, it) },
        )
        toggle(
            title = soundTitle,
            checked = settings[KeyboardToggle.SOUND],
            onCheckedChange = { onToggle(KeyboardToggle.SOUND, it) },
        )
        toggle(
            title = keyPreviewTitle,
            checked = settings[KeyboardToggle.KEY_PREVIEW],
            onCheckedChange = { onToggle(KeyboardToggle.KEY_PREVIEW, it) },
        )
    }
}

/** Скоро: место для функций в разработке — экран под них уже готов. */
@Composable
internal fun SoonSection() {
    val sectionTitle = stringResource(R.string.settings_section_soon)
    val badge = stringResource(R.string.settings_soon_badge)
    val translateIcon = painterResource(R.drawable.ic_settings_translate)
    val voiceIcon = painterResource(R.drawable.ic_settings_voice)
    val translatorTitle = stringResource(R.string.settings_translator_title)
    val translatorDescription = stringResource(R.string.settings_translator_description)
    val voiceTitle = stringResource(R.string.settings_voice_title)

    SettingsSection(title = sectionTitle) {
        soon(
            title = translatorTitle,
            description = translatorDescription,
            icon = translateIcon,
            badge = badge,
        )
        soon(
            title = voiceTitle,
            icon = voiceIcon,
            badge = badge,
        )
    }
}
