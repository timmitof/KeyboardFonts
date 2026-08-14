package kg.timmitof.feature_settings.domain.usecase

import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.repository.KeyboardSettingsRepository

/** Сменить тему клавиатуры. */
class SetKeyboardThemeUseCase(
    private val keyboardSettingsRepository: KeyboardSettingsRepository
) {

    suspend operator fun invoke(mode: KeyboardThemeMode) =
        keyboardSettingsRepository.setTheme(mode)
}
