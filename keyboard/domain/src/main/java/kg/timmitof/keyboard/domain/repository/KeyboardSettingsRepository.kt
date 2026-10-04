package kg.timmitof.keyboard.domain.repository

import kg.timmitof.keyboard.domain.model.KeyColorTarget
import kg.timmitof.keyboard.domain.model.KeyboardBackground
import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardSoundPack
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kotlinx.coroutines.flow.Flow

/** Поток, а не разовое чтение: клавиатура живёт в другом процессе и должна подхватывать правки сразу. */
interface KeyboardSettingsRepository {

    fun observeSettings(): Flow<KeyboardSettings>

    suspend fun getSettings(): KeyboardSettings

    suspend fun setToggle(toggle: KeyboardToggle, enabled: Boolean)

    suspend fun setTheme(mode: KeyboardThemeMode)

    suspend fun setHeight(height: KeyboardHeight)

    suspend fun setKeyColor(target: KeyColorTarget, argb: Long?)

    suspend fun setSoundPack(pack: KeyboardSoundPack)

    suspend fun setSoundVolume(volume: Float)

    /** Новый фон приносит свою палитру, поэтому обычные и служебные клавиши возвращаются к «Авто»; Enter пользователя остаётся. */
    suspend fun setBackground(background: KeyboardBackground)
}
