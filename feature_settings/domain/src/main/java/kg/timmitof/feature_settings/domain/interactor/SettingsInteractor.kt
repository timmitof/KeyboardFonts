package kg.timmitof.feature_settings.domain.interactor

import kg.timmitof.feature_settings.domain.model.SettingsSummary
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardBoard
import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kotlinx.coroutines.flow.Flow

interface SettingsInteractor {

    fun observeSettings(): Flow<KeyboardSettings>

    suspend fun setToggle(toggle: KeyboardToggle, enabled: Boolean)

    suspend fun setTheme(mode: KeyboardThemeMode)

    suspend fun setHeight(height: KeyboardHeight)

    suspend fun getSummary(): SettingsSummary

    fun observeClipboard(): Flow<ClipboardBoard>

    suspend fun clearRecentClipboard()
}
