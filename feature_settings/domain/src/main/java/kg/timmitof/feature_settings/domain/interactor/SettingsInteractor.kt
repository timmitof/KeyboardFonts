package kg.timmitof.feature_settings.domain.interactor

import kg.timmitof.feature_settings.domain.model.SettingsSummary
import kg.timmitof.feature_settings.domain.usecase.GetSettingsSummaryUseCase
import kg.timmitof.feature_settings.domain.usecase.ObserveKeyboardSettingsUseCase
import kg.timmitof.feature_settings.domain.usecase.SetKeyboardThemeUseCase
import kg.timmitof.feature_settings.domain.usecase.SetKeyboardToggleUseCase
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kotlinx.coroutines.flow.Flow

class SettingsInteractor(
    private val observeKeyboardSettingsUseCase: ObserveKeyboardSettingsUseCase,
    private val setKeyboardToggleUseCase: SetKeyboardToggleUseCase,
    private val setKeyboardThemeUseCase: SetKeyboardThemeUseCase,
    private val getSettingsSummaryUseCase: GetSettingsSummaryUseCase,
) {

    fun observeSettings(): Flow<KeyboardSettings> = observeKeyboardSettingsUseCase()

    suspend fun setToggle(toggle: KeyboardToggle, enabled: Boolean) =
        setKeyboardToggleUseCase(toggle, enabled)

    suspend fun setTheme(mode: KeyboardThemeMode) = setKeyboardThemeUseCase(mode)

    suspend fun getSummary(): SettingsSummary = getSettingsSummaryUseCase()
}
