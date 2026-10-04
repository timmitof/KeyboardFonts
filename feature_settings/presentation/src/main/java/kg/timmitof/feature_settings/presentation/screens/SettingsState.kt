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
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle

/**
 * Состояние «Студии» — корневого экрана настроек.
 *
 * @property settings положение всех переключателей, тема и высота.
 * @property summary языки, шрифты, статус клавиатуры и раскладка предпросмотра.
 * @property clipboard история буфера для вкладки «Буфер».
 * @property selectedTab открытая вкладка под предпросмотром.
 */
@Immutable
data class SettingsState(
    val settings: KeyboardSettings = KeyboardSettings(),
    val summary: SettingsSummary = SettingsSummary(),
    val clipboard: ClipboardBoard = ClipboardBoard(),
    val selectedTab: StudioTab = StudioTab.Default,
) : BaseState()

/** Screen-specific one-off effects */
sealed class SettingsSideEffect : BaseSideEffect.UiSideEffect()

/** All actions coming from the UI */
sealed class SettingsEvent : BaseEvent.UiEvent() {

    /** Переключена одна из настроек. */
    data class ToggleChanged(val toggle: KeyboardToggle, val enabled: Boolean) : SettingsEvent()

    /** Выбрана тема клавиатуры. */
    data class ThemeChanged(val mode: KeyboardThemeMode) : SettingsEvent()

    /** Выбрана высота клавиатуры. */
    data class HeightChanged(val height: KeyboardHeight) : SettingsEvent()

    /** Открыта другая вкладка. */
    data class TabSelected(val tab: StudioTab) : SettingsEvent()

    /** «Очистить недавние» во вкладке «Буфер». */
    data object ClearRecentClipboardClicked : SettingsEvent()

    /** Экран вернулся на передний план — статус клавиатуры, язык и шрифт могли измениться. */
    data object ScreenResumed : SettingsEvent()

    /** Пилюля «Не подключена» — ведёт к подключению клавиатуры. */
    data object ConnectKeyboardClicked : SettingsEvent()
}
