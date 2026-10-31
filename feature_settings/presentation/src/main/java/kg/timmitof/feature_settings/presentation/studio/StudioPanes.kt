package kg.timmitof.feature_settings.presentation.studio

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import kg.timmitof.core.resources.MR
import kg.timmitof.core.resources.clipboard_clear_recent
import kg.timmitof.core.resources.clipboard_pinned_empty
import kg.timmitof.core.resources.clipboard_pinned_header
import kg.timmitof.core.resources.clipboard_recent_description
import kg.timmitof.core.resources.clipboard_recent_title
import kg.timmitof.core.resources.font_name_arc
import kg.timmitof.core.resources.font_name_bold
import kg.timmitof.core.resources.font_name_bold_fraktur
import kg.timmitof.core.resources.font_name_bold_italic
import kg.timmitof.core.resources.font_name_bold_script
import kg.timmitof.core.resources.font_name_circled
import kg.timmitof.core.resources.font_name_clouds
import kg.timmitof.core.resources.font_name_default
import kg.timmitof.core.resources.font_name_dot_above
import kg.timmitof.core.resources.font_name_double_struck
import kg.timmitof.core.resources.font_name_double_underline
import kg.timmitof.core.resources.font_name_fraktur
import kg.timmitof.core.resources.font_name_italic
import kg.timmitof.core.resources.font_name_latin_only
import kg.timmitof.core.resources.font_name_monospace
import kg.timmitof.core.resources.font_name_old_slavonic
import kg.timmitof.core.resources.font_name_sans
import kg.timmitof.core.resources.font_name_sans_bold
import kg.timmitof.core.resources.font_name_sans_bold_italic
import kg.timmitof.core.resources.font_name_sans_italic
import kg.timmitof.core.resources.font_name_script
import kg.timmitof.core.resources.font_name_slashed
import kg.timmitof.core.resources.font_name_small_caps
import kg.timmitof.core.resources.font_name_spaced_caps
import kg.timmitof.core.resources.font_name_squared
import kg.timmitof.core.resources.font_name_strikethrough
import kg.timmitof.core.resources.font_name_tilde_above
import kg.timmitof.core.resources.font_name_underline
import kg.timmitof.core.resources.fonts_catalog_header
import kg.timmitof.core.resources.fonts_hidden_header
import kg.timmitof.core.resources.fonts_panel_description
import kg.timmitof.core.resources.fonts_panel_title
import kg.timmitof.core.resources.fonts_remember_description
import kg.timmitof.core.resources.fonts_remember_title
import kg.timmitof.core.resources.fonts_reorder_hint
import kg.timmitof.core.resources.fonts_reset
import kg.timmitof.core.resources.input_autocorrect_description
import kg.timmitof.core.resources.input_autocorrect_title
import kg.timmitof.core.resources.input_learn_description
import kg.timmitof.core.resources.input_learn_title
import kg.timmitof.core.resources.input_next_word_description
import kg.timmitof.core.resources.input_next_word_title
import kg.timmitof.core.resources.input_space_commit_description
import kg.timmitof.core.resources.input_space_commit_title
import kg.timmitof.core.resources.input_suggestions_description
import kg.timmitof.core.resources.input_suggestions_description_off
import kg.timmitof.core.resources.input_suggestions_title
import kg.timmitof.core.resources.size_digits_row_description
import kg.timmitof.core.resources.size_digits_row_title
import kg.timmitof.core.resources.size_height_l
import kg.timmitof.core.resources.size_height_m
import kg.timmitof.core.resources.size_height_s
import kg.timmitof.core.resources.size_height_title
import kg.timmitof.core.resources.size_height_xl
import kg.timmitof.core.resources.size_split_description
import kg.timmitof.core.resources.size_split_title
import kg.timmitof.core.resources.sound_click_title
import kg.timmitof.core.resources.sound_key_preview_description
import kg.timmitof.core.resources.sound_key_preview_title
import kg.timmitof.core.resources.sound_pack_bubble
import kg.timmitof.core.resources.sound_pack_header
import kg.timmitof.core.resources.sound_pack_mechanical
import kg.timmitof.core.resources.sound_pack_system
import kg.timmitof.core.resources.sound_pack_typewriter
import kg.timmitof.core.resources.sound_vibration_title
import kg.timmitof.core.resources.sound_volume_title
import kg.timmitof.core.resources.sound_volume_value
import kg.timmitof.core.resources.theme_auto
import kg.timmitof.core.resources.theme_dark
import kg.timmitof.core.resources.theme_light
import kg.timmitof.core.resources.theme_overridden_by_background
import kg.timmitof.core.ui.components.color.ColorPickerDialog
import kg.timmitof.core.ui.components.settings.SettingsSection
import kg.timmitof.core.ui.components.settings.SettingsSectionAction
import kg.timmitof.core.ui.components.settings.SettingsSectionFooter
import kg.timmitof.core.ui.components.settings.SettingsSectionHeader
import kg.timmitof.core.ui.components.tabs.ChipTab
import kg.timmitof.core.ui.components.tabs.ChipTabs
import kg.timmitof.core.ui.theme.AccentRole
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
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardSoundPack
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kg.timmitof.keyboard.font.domain.model.FontPanel
import kg.timmitof.keyboard.font.domain.model.FontScript
import kg.timmitof.keyboard.font.domain.model.KeyboardFont
import kg.timmitof.keyboard.presentation.components.fontSample
import kg.timmitof.keyboard.presentation.sound.KeySound
import kg.timmitof.keyboard.presentation.sound.rememberKeySoundPlayer
import kg.timmitof.keyboard.presentation.theme.palette
import kotlin.math.roundToInt

/** Ресурсы читаются до DSL: сборщик строк намеренно не композабельный. */
@Composable
internal fun ThemePane(
    settings: KeyboardSettings,
    onTheme: (KeyboardThemeMode) -> Unit,
    onKeyColor: (KeyColorTarget, Long?) -> Unit,
    onToggle: (KeyboardToggle, Boolean) -> Unit,
) {
    val lightLabel = stringResource(MR.strings.theme_light)
    val darkLabel = stringResource(MR.strings.theme_dark)
    val autoLabel = stringResource(MR.strings.theme_auto)
    val tiles = remember(lightLabel, darkLabel, autoLabel) {
        listOf(
            ThemeTile(KeyboardThemeMode.LIGHT, lightLabel),
            ThemeTile(KeyboardThemeMode.DARK, darkLabel),
            ThemeTile(KeyboardThemeMode.AUTO, autoLabel),
        )
    }
    // Фон с палитрой сам решает, светлая основа или тёмная, — говорим об этом прямо под плитками.
    val hasBackgroundPalette = settings.background.palette() != null

    Column {
        ThemeTiles(tiles = tiles, selected = settings.theme, onSelect = onTheme)
        if (hasBackgroundPalette) {
            SettingsSectionFooter(text = stringResource(MR.strings.theme_overridden_by_background))
        }
    }

    KeyColorsSection(settings = settings, onKeyColor = onKeyColor, onToggle = onToggle)
}

@Composable
internal fun FontsPane(
    settings: KeyboardSettings,
    panel: FontPanel,
    script: FontScript,
    onToggle: (KeyboardToggle, Boolean) -> Unit,
    onPanelFonts: (List<String>) -> Unit,
    onReset: () -> Unit,
) {
    val isPanelOn = settings[KeyboardToggle.STYLED_FONTS]

    val panelTitle = stringResource(MR.strings.fonts_panel_title)
    val panelDescription = stringResource(MR.strings.fonts_panel_description)
    val rememberTitle = stringResource(MR.strings.fonts_remember_title)
    val rememberDescription = stringResource(MR.strings.fonts_remember_description)
    val visible = fontItems(panel.visible, script)
    val hidden = fontItems(panel.hidden, script)
    val visibleIds = remember(visible) { visible.map(FontItem::id) }

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
                title = stringResource(MR.strings.fonts_catalog_header, visible.size),
                action = SettingsSectionAction(
                    label = stringResource(MR.strings.fonts_reset),
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
                SettingsSectionFooter(text = stringResource(MR.strings.fonts_reorder_hint))
            }
        }
    }

    if (hidden.isNotEmpty()) {
        Column {
            SettingsSectionHeader(title = stringResource(MR.strings.fonts_hidden_header, hidden.size))
            HiddenFontsList(
                fonts = hidden,
                onShow = { font -> onPanelFonts(visibleIds + font.id) },
            )
        }
    }
}

/**
 * Список собирается один раз на смену набора шрифтов или языка, а не на каждую рекомпозицию.
 * Образец — на алфавите выбранного языка; стиль без его поддержки показывается на латинице с пометкой.
 */
@Composable
private fun fontItems(fonts: List<KeyboardFont>, script: FontScript): List<FontItem> {
    val names = fonts.map { font ->
        val name = font.nameRes?.let { stringResource(it) } ?: font.id
        if (font.supports(script)) name else stringResource(MR.strings.font_name_latin_only, name)
    }
    return remember(fonts, names, script) {
        fonts.mapIndexed { index, font ->
            val sample = if (font.supports(script)) script.fontSample else FontScript.LATIN.fontSample
            FontItem(font.id, names[index], font.apply(sample))
        }
    }
}

private val KeyboardFont.nameRes: StringResource?
    get() = when (id) {
        KeyboardFont.DEFAULT_ID -> MR.strings.font_name_default
        "script" -> MR.strings.font_name_script
        "bold_script" -> MR.strings.font_name_bold_script
        "fraktur" -> MR.strings.font_name_fraktur
        "bold_fraktur" -> MR.strings.font_name_bold_fraktur
        "double_struck" -> MR.strings.font_name_double_struck
        "circled" -> MR.strings.font_name_circled
        "squared" -> MR.strings.font_name_squared
        "small_caps" -> MR.strings.font_name_small_caps
        "bold" -> MR.strings.font_name_bold
        "italic" -> MR.strings.font_name_italic
        "bold_italic" -> MR.strings.font_name_bold_italic
        "sans" -> MR.strings.font_name_sans
        "sans_bold" -> MR.strings.font_name_sans_bold
        "sans_italic" -> MR.strings.font_name_sans_italic
        "sans_bold_italic" -> MR.strings.font_name_sans_bold_italic
        "monospace" -> MR.strings.font_name_monospace
        "underline" -> MR.strings.font_name_underline
        "strikethrough" -> MR.strings.font_name_strikethrough
        "double_underline" -> MR.strings.font_name_double_underline
        "slashed" -> MR.strings.font_name_slashed
        "clouds" -> MR.strings.font_name_clouds
        "arc" -> MR.strings.font_name_arc
        "dot_above" -> MR.strings.font_name_dot_above
        "tilde_above" -> MR.strings.font_name_tilde_above
        "spaced_caps" -> MR.strings.font_name_spaced_caps
        "old_slavonic" -> MR.strings.font_name_old_slavonic
        else -> null
    }

@Composable
internal fun InputPane(
    settings: KeyboardSettings,
    onToggle: (KeyboardToggle, Boolean) -> Unit,
) {
    val isSuggestionsOn = settings[KeyboardToggle.SUGGESTIONS]

    val suggestionsTitle = stringResource(MR.strings.input_suggestions_title)
    val suggestionsDescription = stringResource(
        if (isSuggestionsOn) {
            MR.strings.input_suggestions_description
        } else {
            MR.strings.input_suggestions_description_off
        }
    )
    val autoCorrectTitle = stringResource(MR.strings.input_autocorrect_title)
    val autoCorrectDescription = stringResource(MR.strings.input_autocorrect_description)
    val spaceCommitTitle = stringResource(MR.strings.input_space_commit_title)
    val spaceCommitDescription = stringResource(MR.strings.input_space_commit_description)
    val nextWordTitle = stringResource(MR.strings.input_next_word_title)
    val nextWordDescription = stringResource(MR.strings.input_next_word_description)
    val learnTitle = stringResource(MR.strings.input_learn_title)
    val learnDescription = stringResource(MR.strings.input_learn_description)

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
internal fun SizePane(
    settings: KeyboardSettings,
    onHeight: (KeyboardHeight) -> Unit,
    onToggle: (KeyboardToggle, Boolean) -> Unit,
) {
    val heightTitle = stringResource(MR.strings.size_height_title)
    val heightLabels = KeyboardHeight.entries.map { stringResource(it.labelRes) }
    val heightOptions = remember(heightLabels) { heightLabels }
    val digitsTitle = stringResource(MR.strings.size_digits_row_title)
    val digitsDescription = stringResource(MR.strings.size_digits_row_description)
    val splitTitle = stringResource(MR.strings.size_split_title)
    val splitDescription = stringResource(MR.strings.size_split_description)

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
        toggle(
            title = splitTitle,
            description = splitDescription,
            checked = settings[KeyboardToggle.SPLIT_KEYBOARD],
            onCheckedChange = { onToggle(KeyboardToggle.SPLIT_KEYBOARD, it) },
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
    // Состояние читается только внутри строки-слайдера, поэтому панель не перекомпонуется на каждый кадр.
    val volume = remember(settings.soundVolume) { mutableFloatStateOf(settings.soundVolume) }
    val resources = LocalResources.current

    val vibrationTitle = stringResource(MR.strings.sound_vibration_title)
    val soundTitle = stringResource(MR.strings.sound_click_title)
    val volumeTitle = stringResource(MR.strings.sound_volume_title)
    val packLabels = KeyboardSoundPack.entries.map { stringResource(it.labelRes) }
    val packIcons = KeyboardSoundPack.entries.map { painterResource(it.iconRes) }
    val packs = remember(packLabels, packIcons) {
        KeyboardSoundPack.entries.mapIndexed { index, pack ->
            ChipTab(
                key = pack,
                label = packLabels[index],
                icon = packIcons[index],
                role = AccentRole.HINT,
            )
        }
    }
    val keyPreviewTitle = stringResource(MR.strings.sound_key_preview_title)
    val keyPreviewDescription = stringResource(MR.strings.sound_key_preview_description)

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
            value = volume.floatValue,
            valueLabel = resources.getString(MR.strings.sound_volume_value.resourceId, (volume.floatValue * 100).roundToInt()),
            isNested = true,
            isEnabled = isSoundOn,
            onValueChangeFinished = {
                onSoundVolume(volume.floatValue)
                player.play(KeySound.STANDARD, settings.soundPack, volume.floatValue)
            },
            onValueChange = { volume.floatValue = it },
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
            SettingsSectionHeader(title = stringResource(MR.strings.sound_pack_header))
            ChipTabs(
                tabs = packs,
                selected = settings.soundPack,
                onSelect = { pack ->
                    onSoundPack(pack)
                    player.play(KeySound.STANDARD, pack, volume.floatValue)
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
    val pinnedHeader = stringResource(MR.strings.clipboard_pinned_header)
    val clearLabel = stringResource(MR.strings.clipboard_clear_recent)
    val emptyText = stringResource(MR.strings.clipboard_pinned_empty)
    val recentTitle = stringResource(MR.strings.clipboard_recent_title)
    val recentDescription = stringResource(MR.strings.clipboard_recent_description)

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

private val KeyboardSoundPack.labelRes: StringResource
    get() = when (this) {
        KeyboardSoundPack.SYSTEM -> MR.strings.sound_pack_system
        KeyboardSoundPack.MECHANICAL -> MR.strings.sound_pack_mechanical
        KeyboardSoundPack.BUBBLE -> MR.strings.sound_pack_bubble
        KeyboardSoundPack.TYPEWRITER -> MR.strings.sound_pack_typewriter
    }

private val KeyboardSoundPack.iconRes: Int
    get() = when (this) {
        KeyboardSoundPack.SYSTEM -> R.drawable.ic_sound_system
        KeyboardSoundPack.MECHANICAL -> R.drawable.ic_sound_mechanical
        KeyboardSoundPack.BUBBLE -> R.drawable.ic_sound_bubble
        KeyboardSoundPack.TYPEWRITER -> R.drawable.ic_sound_typewriter
    }

private val KeyboardHeight.labelRes: StringResource
    get() = when (this) {
        KeyboardHeight.S -> MR.strings.size_height_s
        KeyboardHeight.M -> MR.strings.size_height_m
        KeyboardHeight.L -> MR.strings.size_height_l
        KeyboardHeight.XL -> MR.strings.size_height_xl
    }
