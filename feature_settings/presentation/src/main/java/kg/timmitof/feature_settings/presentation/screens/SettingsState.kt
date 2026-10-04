package kg.timmitof.feature_settings.presentation.screens

import androidx.compose.runtime.Immutable
import kg.timmitof.core.ui.base.BaseEvent
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseState
import kg.timmitof.feature_settings.domain.model.SettingsSummary
import kg.timmitof.feature_settings.presentation.studio.StudioTab
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardBoard
import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardSoundPack
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kg.timmitof.keyboard.font.domain.model.FontPanel

@Immutable
data class SettingsState(
    val settings: KeyboardSettings = KeyboardSettings(),
    val summary: SettingsSummary = SettingsSummary(),
    val clipboard: ClipboardBoard = ClipboardBoard(),
    val fontPanel: FontPanel = FontPanel(),
    val selectedTab: StudioTab = StudioTab.Default,
) : BaseState()

sealed class SettingsSideEffect : BaseSideEffect.UiSideEffect()

sealed class SettingsEvent : BaseEvent.UiEvent() {

    data class ToggleChanged(val toggle: KeyboardToggle, val enabled: Boolean) : SettingsEvent()

    data class ThemeChanged(val mode: KeyboardThemeMode) : SettingsEvent()

    data class HeightChanged(val height: KeyboardHeight) : SettingsEvent()

    data class EnterColorChanged(val argb: Long?) : SettingsEvent()

    data class SoundPackChanged(val pack: KeyboardSoundPack) : SettingsEvent()

    data class SoundVolumeChanged(val volume: Float) : SettingsEvent()

    data class PanelFontsChanged(val ids: List<String>) : SettingsEvent()

    data object ResetFontPanelClicked : SettingsEvent()

    data class TabSelected(val tab: StudioTab) : SettingsEvent()

    data object ClearRecentClipboardClicked : SettingsEvent()

    data object ScreenResumed : SettingsEvent()

    data object ConnectKeyboardClicked : SettingsEvent()
}
