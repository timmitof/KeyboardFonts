package kg.timmitof.feature_settings.presentation.studio

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kg.timmitof.core.ui.components.color.ColorPickerDialog
import kg.timmitof.core.ui.components.settings.SettingsSection
import kg.timmitof.core.ui.components.tabs.ChipTab
import kg.timmitof.core.ui.components.tabs.ChipTabs
import kg.timmitof.core.ui.theme.AccentRole
import kg.timmitof.core.ui.components.settings.SettingsSectionAction
import kg.timmitof.core.ui.components.settings.SettingsSectionFooter
import kg.timmitof.core.ui.components.settings.SettingsSectionHeader
import kg.timmitof.feature_settings.presentation.R
import kg.timmitof.feature_settings.presentation.components.ClipboardPins
import kg.timmitof.feature_settings.presentation.components.FontItem
import kg.timmitof.feature_settings.presentation.components.HiddenFontsList
import kg.timmitof.feature_settings.presentation.components.ThemeTile
import kg.timmitof.feature_settings.presentation.components.ThemeTiles
import kg.timmitof.feature_settings.presentation.components.VisibleFontsList
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardBoard
import kg.timmitof.keyboard.domain.model.KeyColorTarget
import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardSoundPack
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kg.timmitof.keyboard.font.domain.model.FontPanel
import kg.timmitof.keyboard.font.domain.model.KeyboardFont
import kg.timmitof.keyboard.presentation.sound.KeySound
import kg.timmitof.keyboard.presentation.theme.palette
import kg.timmitof.keyboard.presentation.sound.rememberKeySoundPlayer
import kotlin.math.roundToInt

/** Ресурсы читаются до DSL: сборщик строк намеренно не композабельный. */
@Composable
internal fun ThemePane(
    settings: KeyboardSettings,
    onTheme: (KeyboardThemeMode) -> Unit,
    onKeyColor: (KeyColorTarget, Long?) -> Unit,
    onToggle: (KeyboardToggle, Boolean) -> Unit,
) {
    val tiles = listOf(
        ThemeTile(KeyboardThemeMode.LIGHT, stringResource(R.string.theme_light)),
        ThemeTile(KeyboardThemeMode.DARK, stringResource(R.string.theme_dark)),
        ThemeTile(KeyboardThemeMode.AUTO, stringResource(R.string.theme_auto)),
    )
    // Фон с палитрой сам решает, светлая основа или тёмная, — говорим об этом прямо под плитками.
    val hasBackgroundPalette = settings.background.palette() != null

    Column {
        ThemeTiles(tiles = tiles, selected = settings.theme, onSelect = onTheme)
        if (hasBackgroundPalette) {
            SettingsSectionFooter(text = stringResource(R.string.theme_overridden_by_background))
        }
    }

    KeyColorsSection(settings = settings, onKeyColor = onKeyColor, onToggle = onToggle)
}

@Composable
internal fun FontsPane(
    settings: KeyboardSettings,
    panel: FontPanel,
    onToggle: (KeyboardToggle, Boolean) -> Unit,
    onPanelFonts: (List<String>) -> Unit,
    onReset: () -> Unit,
) {
    val isPanelOn = settings[KeyboardToggle.STYLED_FONTS]

    val panelTitle = stringResource(R.string.fonts_panel_title)
    val panelDescription = stringResource(R.string.fonts_panel_description)
    val rememberTitle = stringResource(R.string.fonts_remember_title)
    val rememberDescription = stringResource(R.string.fonts_remember_description)
    val visible = panel.visible.map { fontItem(it) }
    val hidden = panel.hidden.map { fontItem(it) }
    val visibleIds = visible.map(FontItem::id)

    SettingsSection {
        toggle(
            title = panelTitle,
            description = panelDescription,
            checked = isPanelOn,
            onCheckedChange = { onToggle(KeyboardToggle.STYLED_FONTS, it) },
        )
        toggle(
            title = rememberTitle,
            description = rememberDescription,
            isNested = true,
            isEnabled = isPanelOn,
            checked = settings[KeyboardToggle.REMEMBER_FONT],
            onCheckedChange = { onToggle(KeyboardToggle.REMEMBER_FONT, it) },
        )
    }

    if (visible.isNotEmpty()) {
        Column {
            SettingsSectionHeader(
                title = stringResource(R.string.fonts_catalog_header, visible.size),
                action = SettingsSectionAction(
                    label = stringResource(R.string.fonts_reset),
                    isEnabled = panel.isCustom,
                    onClick = onReset,
                ),
            )
            // Последний шрифт не прячем: иначе панель над клавишами опустеет.
            VisibleFontsList(
                fonts = visible,
                canHide = visible.size > 1,
                onHide = { font -> onPanelFonts(visibleIds - font.id) },
                onReorder = { fonts -> onPanelFonts(fonts.map(FontItem::id)) },
            )
            if (visible.size > 1) {
                SettingsSectionFooter(text = stringResource(R.string.fonts_reorder_hint))
            }
        }
    }

    if (hidden.isNotEmpty()) {
        Column {
            SettingsSectionHeader(title = stringResource(R.string.fonts_hidden_header, hidden.size))
            HiddenFontsList(
                fonts = hidden,
                onShow = { font -> onPanelFonts(visibleIds + font.id) },
            )
        }
    }
}

@Composable
private fun fontItem(font: KeyboardFont) = FontItem(
    id = font.id,
    name = font.nameRes?.let { stringResource(it) } ?: font.id,
    sample = remember(font) { font.apply(FontSample) },
)

private const val FontSample = "Abc"

private val KeyboardFont.nameRes: Int?
    get() = when (id) {
        KeyboardFont.DEFAULT_ID -> R.string.font_name_default
        "script" -> R.string.font_name_script
        "bold_script" -> R.string.font_name_bold_script
        "fraktur" -> R.string.font_name_fraktur
        "bold_fraktur" -> R.string.font_name_bold_fraktur
        "double_struck" -> R.string.font_name_double_struck
        "circled" -> R.string.font_name_circled
        "squared" -> R.string.font_name_squared
        "small_caps" -> R.string.font_name_small_caps
        "bold" -> R.string.font_name_bold
        "italic" -> R.string.font_name_italic
        "bold_italic" -> R.string.font_name_bold_italic
        "sans" -> R.string.font_name_sans
        "sans_bold" -> R.string.font_name_sans_bold
        "sans_italic" -> R.string.font_name_sans_italic
        "sans_bold_italic" -> R.string.font_name_sans_bold_italic
        "monospace" -> R.string.font_name_monospace
        "underline" -> R.string.font_name_underline
        "strikethrough" -> R.string.font_name_strikethrough
        else -> null
    }

@Composable
internal fun InputPane(
    settings: KeyboardSettings,
    onToggle: (KeyboardToggle, Boolean) -> Unit,
) {
    val isSuggestionsOn = settings[KeyboardToggle.SUGGESTIONS]

    val suggestionsTitle = stringResource(R.string.input_suggestions_title)
    val suggestionsDescription = stringResource(
        if (isSuggestionsOn) {
            R.string.input_suggestions_description
        } else {
            R.string.input_suggestions_description_off
        }
    )
    val autoCorrectTitle = stringResource(R.string.input_autocorrect_title)
    val autoCorrectDescription = stringResource(R.string.input_autocorrect_description)
    val spaceCommitTitle = stringResource(R.string.input_space_commit_title)
    val spaceCommitDescription = stringResource(R.string.input_space_commit_description)
    val nextWordTitle = stringResource(R.string.input_next_word_title)
    val nextWordDescription = stringResource(R.string.input_next_word_description)
    val learnTitle = stringResource(R.string.input_learn_title)
    val learnDescription = stringResource(R.string.input_learn_description)

    SettingsSection {
        toggle(
            title = suggestionsTitle,
            description = suggestionsDescription,
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
            description = nextWordDescription,
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

@Composable
internal fun LanguagesPane(
    languages: List<KeyboardLanguage>,
    selected: KeyboardLanguage?,
) {
    val header = stringResource(R.string.languages_header)
    val current = stringResource(R.string.languages_current)

    SettingsSection(title = header) {
        languages.forEach { language ->
            info(
                title = language.displayName,
                badge = language.shortName,
                description = current.takeIf { language.code == selected?.code },
            )
        }
    }
}

@Composable
internal fun SizePane(
    settings: KeyboardSettings,
    onHeight: (KeyboardHeight) -> Unit,
    onToggle: (KeyboardToggle, Boolean) -> Unit,
) {
    val heightTitle = stringResource(R.string.size_height_title)
    val heightOptions = KeyboardHeight.entries.map { stringResource(it.labelRes) }
    val digitsTitle = stringResource(R.string.size_digits_row_title)
    val digitsDescription = stringResource(R.string.size_digits_row_description)

    SettingsSection {
        segmented(
            title = heightTitle,
            options = heightOptions,
            selectedIndex = settings.height.ordinal,
            onSelect = { onHeight(KeyboardHeight.entries[it]) },
        )
        toggle(
            title = digitsTitle,
            description = digitsDescription,
            checked = settings[KeyboardToggle.DIGITS_ROW],
            onCheckedChange = { onToggle(KeyboardToggle.DIGITS_ROW, it) },
        )
    }
}

@Composable
internal fun SoundPane(
    settings: KeyboardSettings,
    onToggle: (KeyboardToggle, Boolean) -> Unit,
    onSoundPack: (KeyboardSoundPack) -> Unit,
    onSoundVolume: (Float) -> Unit,
) {
    val player = rememberKeySoundPlayer()
    val isSoundOn = settings[KeyboardToggle.SOUND]

    // Пока тянут ползунок, громкость живёт локально, в хранилище — по отпусканию.
    var volume by remember(settings.soundVolume) { mutableFloatStateOf(settings.soundVolume) }

    val vibrationTitle = stringResource(R.string.sound_vibration_title)
    val soundTitle = stringResource(R.string.sound_click_title)
    val volumeTitle = stringResource(R.string.sound_volume_title)
    val volumeLabel = stringResource(R.string.sound_volume_value, (volume * 100).roundToInt())
    val packs = KeyboardSoundPack.entries.map { pack ->
        ChipTab(
            key = pack,
            label = stringResource(pack.labelRes),
            icon = painterResource(pack.iconRes),
            role = AccentRole.HINT,
        )
    }
    val keyPreviewTitle = stringResource(R.string.sound_key_preview_title)
    val keyPreviewDescription = stringResource(R.string.sound_key_preview_description)

    SettingsSection {
        toggle(
            title = vibrationTitle,
            checked = settings[KeyboardToggle.VIBRATION],
            onCheckedChange = { onToggle(KeyboardToggle.VIBRATION, it) },
        )
        toggle(
            title = soundTitle,
            checked = isSoundOn,
            onCheckedChange = { onToggle(KeyboardToggle.SOUND, it) },
        )
        slider(
            title = volumeTitle,
            value = volume,
            valueLabel = volumeLabel,
            isNested = true,
            isEnabled = isSoundOn,
            onValueChangeFinished = {
                onSoundVolume(volume)
                player.play(KeySound.STANDARD, settings.soundPack, volume)
            },
            onValueChange = { volume = it },
        )
        toggle(
            title = keyPreviewTitle,
            description = keyPreviewDescription,
            checked = settings[KeyboardToggle.KEY_PREVIEW],
            onCheckedChange = { onToggle(KeyboardToggle.KEY_PREVIEW, it) },
        )
    }

    if (isSoundOn) {
        Column {
            SettingsSectionHeader(title = stringResource(R.string.sound_pack_header))
            ChipTabs(
                tabs = packs,
                selected = settings.soundPack,
                onSelect = { pack ->
                    onSoundPack(pack)
                    player.play(KeySound.STANDARD, pack, volume)
                },
                contentPadding = PaddingValues(0.dp),
            )
        }
    }
}

@Composable
internal fun ClipboardPane(
    board: ClipboardBoard,
    onClearRecent: () -> Unit,
) {
    val pinnedHeader = stringResource(R.string.clipboard_pinned_header)
    val clearLabel = stringResource(R.string.clipboard_clear_recent)
    val emptyText = stringResource(R.string.clipboard_pinned_empty)
    val recentTitle = stringResource(R.string.clipboard_recent_title)
    val recentDescription = stringResource(R.string.clipboard_recent_description)

    Column {
        SettingsSectionHeader(
            title = pinnedHeader,
            action = SettingsSectionAction(
                label = clearLabel,
                isEnabled = board.hasClearable,
                onClick = onClearRecent,
            ),
        )
        ClipboardPins(entries = board.pinned, emptyText = emptyText)
    }

    SettingsSection {
        info(
            title = recentTitle,
            description = recentDescription,
            badge = board.recent.size.toString(),
        )
    }
}

private val KeyboardSoundPack.labelRes: Int
    get() = when (this) {
        KeyboardSoundPack.SYSTEM -> R.string.sound_pack_system
        KeyboardSoundPack.MECHANICAL -> R.string.sound_pack_mechanical
        KeyboardSoundPack.BUBBLE -> R.string.sound_pack_bubble
        KeyboardSoundPack.TYPEWRITER -> R.string.sound_pack_typewriter
    }

private val KeyboardSoundPack.iconRes: Int
    get() = when (this) {
        KeyboardSoundPack.SYSTEM -> R.drawable.ic_sound_system
        KeyboardSoundPack.MECHANICAL -> R.drawable.ic_sound_mechanical
        KeyboardSoundPack.BUBBLE -> R.drawable.ic_sound_bubble
        KeyboardSoundPack.TYPEWRITER -> R.drawable.ic_sound_typewriter
    }

private val KeyboardHeight.labelRes: Int
    get() = when (this) {
        KeyboardHeight.S -> R.string.size_height_s
        KeyboardHeight.M -> R.string.size_height_m
        KeyboardHeight.L -> R.string.size_height_l
        KeyboardHeight.XL -> R.string.size_height_xl
    }
