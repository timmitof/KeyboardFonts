package kg.timmitof.feature_settings.domain.usecase

import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kg.timmitof.keyboard.domain.repository.KeyboardSettingsRepository

/** Переключить одну настройку. */
class SetKeyboardToggleUseCase(
    private val keyboardSettingsRepository: KeyboardSettingsRepository
) {

    suspend operator fun invoke(toggle: KeyboardToggle, enabled: Boolean) =
        keyboardSettingsRepository.setToggle(toggle, enabled)
}
