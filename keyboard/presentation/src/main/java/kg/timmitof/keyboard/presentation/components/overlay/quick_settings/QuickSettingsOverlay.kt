package kg.timmitof.keyboard.presentation.components.overlay.quick_settings

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.presentation.components.overlay.OverlayActionRow
import kg.timmitof.keyboard.presentation.components.overlay.OverlayDivider
import kg.timmitof.keyboard.presentation.components.overlay.OverlayHeader
import kg.timmitof.keyboard.presentation.components.overlay.OverlayRows
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
import kg.timmitof.keyboard.presentation.screens.keyboard.states.QuickSetting

/**
 * Быстрые настройки
 */
@Composable
internal fun ColumnScope.QuickSettingsOverlay(
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit,
) {
    val settings = state.value.settings
    val apply = { setting: QuickSetting -> onEvent(KeyboardEvent.OnQuickSetting(setting)) }

    OverlayHeader(
        title = stringResource(R.string.quick_settings_title),
        onClose = { onEvent(KeyboardEvent.OnOverlayChange(null)) },
    )

    val suggestionsTitle = stringResource(R.string.quick_settings_suggestions)
    val fontsTitle = stringResource(R.string.quick_settings_fonts)
    val vibrationTitle = stringResource(R.string.quick_settings_vibration)
    val heightTitle = stringResource(R.string.quick_settings_height)
    val themeTitle = stringResource(R.string.quick_settings_theme)
    val heightOptions = KeyboardHeight.entries.map { stringResource(it.labelRes) }
    val themeOptions = KeyboardThemeMode.entries.map { stringResource(it.labelRes) }

    OverlayRows(modifier = Modifier.weight(1f)) {
        toggle(
            iconRes = R.drawable.ic_quick_suggestions,
            title = suggestionsTitle,
            isChecked = settings[KeyboardToggle.SUGGESTIONS],
            onCheckedChange = { apply(QuickSetting.Toggle(KeyboardToggle.SUGGESTIONS, it)) },
        )

        toggle(
            iconRes = R.drawable.ic_quick_fonts,
            title = fontsTitle,
            isChecked = settings[KeyboardToggle.STYLED_FONTS],
            onCheckedChange = { apply(QuickSetting.Toggle(KeyboardToggle.STYLED_FONTS, it)) },
        )

        toggle(
            iconRes = R.drawable.ic_quick_vibration,
            title = vibrationTitle,
            isChecked = settings[KeyboardToggle.VIBRATION],
            onCheckedChange = { apply(QuickSetting.Toggle(KeyboardToggle.VIBRATION, it)) },
        )

        segmented(
            iconRes = R.drawable.ic_quick_height,
            title = heightTitle,
            options = heightOptions,
            selectedIndex = settings.height.ordinal,
            onSelect = { apply(QuickSetting.Height(KeyboardHeight.entries[it])) },
        )

        segmented(
            iconRes = R.drawable.ic_quick_theme,
            title = themeTitle,
            options = themeOptions,
            selectedIndex = settings.theme.ordinal,
            onSelect = { apply(QuickSetting.Theme(KeyboardThemeMode.entries[it])) }
        )
    }

    OverlayDivider()

    OverlayActionRow(
        label = stringResource(R.string.quick_settings_open_app),
        onClick = { onEvent(KeyboardEvent.OnOpenApp) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
    )
}

private val KeyboardHeight.labelRes: Int
    get() = when (this) {
        KeyboardHeight.S -> R.string.keyboard_height_s
        KeyboardHeight.M -> R.string.keyboard_height_m
        KeyboardHeight.L -> R.string.keyboard_height_l
        KeyboardHeight.XL -> R.string.keyboard_height_xl
    }

private val KeyboardThemeMode.labelRes: Int
    get() = when (this) {
        KeyboardThemeMode.AUTO -> R.string.keyboard_theme_auto
        KeyboardThemeMode.LIGHT -> R.string.keyboard_theme_light
        KeyboardThemeMode.DARK -> R.string.keyboard_theme_dark
    }
