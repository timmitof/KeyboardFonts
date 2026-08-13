package kg.timmitof.keyboard.data.repository

import kg.timmitof.keyboard.data.settings.KeyboardSettingsDataSource
import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kg.timmitof.keyboard.domain.repository.KeyboardSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class KeyboardSettingsRepositoryImpl @Inject constructor(
    private val dataSource: KeyboardSettingsDataSource,
) : KeyboardSettingsRepository {

    /**
     * DataStore присылает весь набор настроек на любую правку — в том числе на
     * чужие ключи вроде выбранного шрифта. [distinctUntilChanged] отсекает
     * повторы, чтобы клавиатура не пересобирала состояние из-за смены шрифта.
     */
    override fun observeSettings(): Flow<KeyboardSettings> =
        dataSource.observe().distinctUntilChanged()

    override suspend fun getSettings(): KeyboardSettings = dataSource.get()

    override suspend fun setToggle(toggle: KeyboardToggle, enabled: Boolean) =
        dataSource.setToggle(toggle, enabled)

    override suspend fun setTheme(mode: KeyboardThemeMode) = dataSource.setTheme(mode)

    override suspend fun setHeight(height: KeyboardHeight) = dataSource.setHeight(height)
}
