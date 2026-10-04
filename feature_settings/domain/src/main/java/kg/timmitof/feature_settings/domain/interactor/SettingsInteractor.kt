package kg.timmitof.feature_settings.domain.interactor

import kg.timmitof.feature_settings.domain.model.SettingsSummary
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardBoard
import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kotlinx.coroutines.flow.Flow

/**
 * Всё, что нужно «Студии»: настройки клавиатуры, сводка вокруг них и буфер.
 */
interface SettingsInteractor {

    /** Настройки клавиатуры и все их изменения — их правит и приложение, и сама клавиатура. */
    fun observeSettings(): Flow<KeyboardSettings>

    /** Переключить одну настройку. */
    suspend fun setToggle(toggle: KeyboardToggle, enabled: Boolean)

    /** Сменить тему клавиатуры. */
    suspend fun setTheme(mode: KeyboardThemeMode)

    /** Сменить высоту рядов клавиш. */
    suspend fun setHeight(height: KeyboardHeight)

    /** Языки, шрифты, статус клавиатуры и раскладка для предпросмотра. */
    suspend fun getSummary(): SettingsSummary

    /** История буфера: закреплённые и недавние записи. */
    fun observeClipboard(): Flow<ClipboardBoard>

    /** Очистить недавние записи буфера — закреплённые остаются. */
    suspend fun clearRecentClipboard()
}
