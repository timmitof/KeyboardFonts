package kg.timmitof.keyboard.domain.repository

import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kotlinx.coroutines.flow.Flow

/**
 * Настройки клавиатуры: их правит экран настроек, а читает сама клавиатура.
 *
 * Поток, а не разовое чтение: клавиатура живёт в другом процессе-сервисе и должна
 * подхватывать изменения сразу, не дожидаясь перезапуска.
 */
interface KeyboardSettingsRepository {

    /** Текущие настройки и все последующие изменения. */
    fun observeSettings(): Flow<KeyboardSettings>

    /** Разовое чтение — для старта клавиатуры. */
    suspend fun getSettings(): KeyboardSettings

    suspend fun setToggle(toggle: KeyboardToggle, enabled: Boolean)

    suspend fun setTheme(mode: KeyboardThemeMode)

    suspend fun setHeight(height: KeyboardHeight)
}
