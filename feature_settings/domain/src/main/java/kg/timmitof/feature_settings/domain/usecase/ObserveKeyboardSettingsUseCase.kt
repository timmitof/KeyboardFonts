package kg.timmitof.feature_settings.domain.usecase

import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.repository.KeyboardSettingsRepository
import kotlinx.coroutines.flow.Flow

/** Настройки клавиатуры и все их изменения. */
class ObserveKeyboardSettingsUseCase(
    private val keyboardSettingsRepository: KeyboardSettingsRepository
) {

    operator fun invoke(): Flow<KeyboardSettings> = keyboardSettingsRepository.observeSettings()
}
