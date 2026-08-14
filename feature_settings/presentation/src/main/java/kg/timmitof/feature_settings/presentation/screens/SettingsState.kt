package kg.timmitof.feature_settings.presentation.screens

import androidx.compose.runtime.Immutable
import kg.timmitof.core.ui.base.BaseEvent
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseState
import kg.timmitof.feature_settings.domain.model.SettingsSummary
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle

/**
 * Состояние экрана настроек.
 *
 * @property settings положение всех переключателей и выбранная тема.
 * @property summary значения строк-переходов: раскладки, шрифты, статус клавиатуры.
 */
@Immutable
data class SettingsState(
    val settings: KeyboardSettings = KeyboardSettings(),
    val summary: SettingsSummary = SettingsSummary(),
) : BaseState()

/** Screen-specific one-off effects */
sealed class SettingsSideEffect : BaseSideEffect.UiSideEffect()

/** All actions coming from the UI */
sealed class SettingsEvent : BaseEvent.UiEvent() {

    /** Переключена одна из настроек. */
    data class ToggleChanged(val toggle: KeyboardToggle, val enabled: Boolean) : SettingsEvent()

    /** Выбрана тема клавиатуры. */
    data class ThemeChanged(val mode: KeyboardThemeMode) : SettingsEvent()

    /** Экран вернулся на передний план — статус клавиатуры мог измениться в системе. */
    data object ScreenResumed : SettingsEvent()

    /** Переход на экран проверки клавиатуры. */
    data object CheckKeyboardClicked : SettingsEvent()
}
