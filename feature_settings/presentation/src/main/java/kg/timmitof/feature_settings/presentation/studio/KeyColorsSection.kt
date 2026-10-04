package kg.timmitof.feature_settings.presentation.studio

import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import kg.timmitof.core.ui.components.color.ColorPickerDialog
import kg.timmitof.core.ui.components.settings.SettingsSection
import kg.timmitof.feature_settings.presentation.R
import kg.timmitof.keyboard.domain.model.KeyColorTarget
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kg.timmitof.keyboard.presentation.theme.autoKeyColor

/** Первый кружок каждой строки — «Авто»: цвет, подобранный под фон, или цвет темы. */
@Composable
internal fun KeyColorsSection(
    settings: KeyboardSettings,
    onKeyColor: (KeyColorTarget, Long?) -> Unit,
    onToggle: ((KeyboardToggle, Boolean) -> Unit)? = null,
) {
    val isSystemDark = isSystemInDarkTheme()
    var pickerTarget by rememberSaveable { mutableStateOf<KeyColorTarget?>(null) }

    val autoLabel = stringResource(R.string.key_color_auto)
    val rows = KeyColorTarget.entries.map { target ->
        KeyColorRow(
            target = target,
            title = stringResource(target.titleRes),
            auto = remember(settings, isSystemDark) { settings.autoKeyColor(target, isSystemDark) },
            selected = settings.keyColor(target)?.let { Color(it.toInt()) },
        )
    }
    val outlineTitle = stringResource(R.string.theme_key_outline_title)
    val outlineDescription = stringResource(R.string.theme_key_outline_description)

    SettingsSection(title = stringResource(R.string.key_colors_header)) {
        rows.forEach { row ->
            colors(
                title = row.title,
                colors = row.target.presets,
                selected = row.selected,
                autoColor = row.auto,
                autoLabel = autoLabel,
                onAuto = { onKeyColor(row.target, null) },
                onPickCustom = { pickerTarget = row.target },
                onSelect = { onKeyColor(row.target, it.toArgbLong()) },
            )
        }
        onToggle?.let { toggleOutline ->
            toggle(
                title = outlineTitle,
                description = outlineDescription,
                checked = settings[KeyboardToggle.KEY_OUTLINE],
                onCheckedChange = { toggleOutline(KeyboardToggle.KEY_OUTLINE, it) },
            )
        }
    }

    pickerTarget?.let { target ->
        val row = rows.first { it.target == target }
        ColorPickerDialog(
            initial = row.selected ?: row.auto,
            title = stringResource(target.pickerTitleRes),
            confirmLabel = stringResource(R.string.color_picker_confirm),
            dismissLabel = stringResource(R.string.color_picker_dismiss),
            onConfirm = { color ->
                pickerTarget = null
                onKeyColor(target, color.toArgbLong())
            },
            onDismiss = { pickerTarget = null },
        )
    }
}

private class KeyColorRow(
    val target: KeyColorTarget,
    val title: String,
    val auto: Color,
    val selected: Color?,
)

private fun Color.toArgbLong(): Long = toArgb().toLong() and 0xFFFFFFFFL

@get:StringRes
private val KeyColorTarget.titleRes: Int
    get() = when (this) {
        KeyColorTarget.KEY -> R.string.key_color_letters
        KeyColorTarget.SPECIAL -> R.string.key_color_special
        KeyColorTarget.ENTER -> R.string.key_color_enter
    }

@get:StringRes
private val KeyColorTarget.pickerTitleRes: Int
    get() = when (this) {
        KeyColorTarget.KEY -> R.string.key_color_letters_picker
        KeyColorTarget.SPECIAL -> R.string.key_color_special_picker
        KeyColorTarget.ENTER -> R.string.key_color_enter_picker
    }

private val KeyColorTarget.presets: List<Color>
    get() = when (this) {
        KeyColorTarget.KEY -> KeyPresets
        KeyColorTarget.SPECIAL -> SpecialPresets
        KeyColorTarget.ENTER -> EnterPresets
    }

private val KeyPresets = listOf(
    Color(0xFFFFFFFF),
    Color(0xFFF3EDF7),
    Color(0xFFE3F4EF),
    Color(0xFFFFF4E5),
    Color(0xFF2D282E),
)

private val SpecialPresets = listOf(
    Color(0xFFBFC4CB),
    Color(0xFFE3B7F3),
    Color(0xFFA8E3D6),
    Color(0xFFFFD08A),
    Color(0xFF3A333B),
)

private val EnterPresets = listOf(
    Color(0xFF6D4BD8),
    Color(0xFF0F7B6C),
    Color(0xFF8A5200),
    Color(0xFF815251),
    Color(0xFF1E1A1F),
)
