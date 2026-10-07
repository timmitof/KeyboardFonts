package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kg.timmitof.keyboard.domain.repository.KeyboardSettingsRepository
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax
import kotlinx.coroutines.flow.Flow

/** Экран настроек живёт в другом процессе, поэтому значения приходят потоком, а не читаются один раз. */
internal class SettingsDelegate(
    private val keyboardSettingsRepository: KeyboardSettingsRepository,
) {

    val settings: Flow<KeyboardSettings> = keyboardSettingsRepository.observeSettings()

    suspend fun KeyboardSyntax.loadSettings() =
        applySettings(keyboardSettingsRepository.getSettings())

    /** Панель шрифтов при выключении сворачивается сразу, иначе висела бы до следующей сессии ввода. */
    suspend fun KeyboardSyntax.applySettings(settings: KeyboardSettings) {
        if (state.settings == settings) return

        reduce {
            state.copy(
                settings = settings,
                isFontsExpanded = state.isFontsExpanded && settings.isFontsPanelEnabled,
            )
        }
    }

    suspend fun setToggle(toggle: KeyboardToggle, isEnabled: Boolean) =
        keyboardSettingsRepository.setToggle(toggle, isEnabled)

    suspend fun setHeight(height: KeyboardHeight) = keyboardSettingsRepository.setHeight(height)

    suspend fun setTheme(theme: KeyboardThemeMode) =
        keyboardSettingsRepository.setTheme(theme)
}
