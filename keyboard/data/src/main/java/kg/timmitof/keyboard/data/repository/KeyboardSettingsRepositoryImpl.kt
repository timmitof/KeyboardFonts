package kg.timmitof.keyboard.data.repository

import kg.timmitof.keyboard.data.settings.KeyboardSettingsDataSource
import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardSoundPack
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kg.timmitof.keyboard.domain.repository.KeyboardSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class KeyboardSettingsRepositoryImpl @Inject constructor(
    private val dataSource: KeyboardSettingsDataSource,
) : KeyboardSettingsRepository {

    /** DataStore шлёт набор на любую правку (в т.ч. смену шрифта) — [distinctUntilChanged] отсекает повторы. */
    override fun observeSettings(): Flow<KeyboardSettings> =
        dataSource.observe().distinctUntilChanged()

    override suspend fun getSettings(): KeyboardSettings = dataSource.get()

    override suspend fun setToggle(toggle: KeyboardToggle, enabled: Boolean) =
        dataSource.setToggle(toggle, enabled)

    override suspend fun setTheme(mode: KeyboardThemeMode) = dataSource.setTheme(mode)

    override suspend fun setHeight(height: KeyboardHeight) = dataSource.setHeight(height)

    override suspend fun setEnterColor(argb: Long?) = dataSource.setEnterColor(argb)

    override suspend fun setSoundPack(pack: KeyboardSoundPack) = dataSource.setSoundPack(pack)

    override suspend fun setSoundVolume(volume: Float) = dataSource.setSoundVolume(volume)
}
