package kg.timmitof.feature_home.domain.usecase

import kg.timmitof.feature_home.domain.model.KeyboardSetupStep
import kg.timmitof.keyboard.integration.KeyboardContract

/** Отправляет пользователя в системный экран, соответствующий шагу [KeyboardSetupStep]. */
class OpenKeyboardSetupUseCase(
    private val keyboardContract: KeyboardContract
) {

    operator fun invoke(step: KeyboardSetupStep) = when (step) {
        KeyboardSetupStep.ENABLE -> keyboardContract.openKeyboardSettings()
        KeyboardSetupStep.SELECT -> keyboardContract.showKeyboardPicker()
        KeyboardSetupStep.DONE -> Unit
    }
}
