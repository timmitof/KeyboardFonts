package kg.timmitof.feature_settings.domain.interactor

import kg.timmitof.feature_settings.domain.model.SettingsSummary
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardBoard
import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kg.timmitof.keyboard.font.domain.model.FontPanel
import kotlinx.coroutines.flow.Flow

interface SettingsInteractor {

    fun observeSettings(): Flow<KeyboardSettings>

    suspend fun setToggle(toggle: KeyboardToggle, enabled: Boolean)

    suspend fun setTheme(mode: KeyboardThemeMode)

    suspend fun setHeight(height: KeyboardHeight)

    suspend fun setEnterColor(argb: Long?)

    suspend fun getSummary(): SettingsSummary

    fun observeFontPanel(): Flow<FontPanel>

    suspend fun setPanelFonts(ids: List<String>)

    suspend fun resetFontPanel()

    fun observeClipboard(): Flow<ClipboardBoard>

    suspend fun clearRecentClipboard()
}
