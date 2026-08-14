package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kg.timmitof.keyboard.domain.repository.KeyboardSettingsRepository
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax
import kotlinx.coroutines.flow.Flow

/**
 * Настройки клавиатуры внутри самой клавиатуры.
 *
 * Экран настроек живёт в другом процессе, поэтому значения не читаются один раз
 * на старте, а приходят потоком: переключатель сработал — клавиатура уже другая.
 */
internal class SettingsDelegate(
    private val keyboardSettingsRepository: KeyboardSettingsRepository,
) {

    val settings: Flow<KeyboardSettings> = keyboardSettingsRepository.observeSettings()

    /** Первое чтение — до него клавиатура рисуется значениями по умолчанию. */
    suspend fun KeyboardSyntax.loadSettings() =
        applySettings(keyboardSettingsRepository.getSettings())

    /**
     * Кладёт настройки в состояние.
     *
     * Панель шрифтов при выключении сворачивается сразу: иначе она осталась бы
     * висеть над клавишами до следующей сессии ввода.
     */
    suspend fun KeyboardSyntax.applySettings(settings: KeyboardSettings) {
        if (state.settings == settings) return

        reduce {
            state.copy(
                settings = settings,
                isFontsExpanded = state.isFontsExpanded && settings.isFontsPanelEnabled,
            )
        }
    }

    /**
     * Правки из листа быстрых настроек: значение уходит в хранилище и приходит
     * обратно потоком — той же дорогой, что и правка с экрана приложения.
     */
    suspend fun setToggle(toggle: KeyboardToggle, isEnabled: Boolean) =
        keyboardSettingsRepository.setToggle(toggle, isEnabled)

    suspend fun setHeight(height: KeyboardHeight) = keyboardSettingsRepository.setHeight(height)

    suspend fun setTheme(theme: KeyboardThemeMode) =
        keyboardSettingsRepository.setTheme(theme)
}
