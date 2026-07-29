package kg.timmitof.feature_home.domain.usecase

import kg.timmitof.feature_home.domain.model.KeyboardSetupModel
import kg.timmitof.keyboard.integration.KeyboardContract

/** Текущее состояние подключения клавиатуры. */
class GetKeyboardSetupUseCase(
    private val keyboardContract: KeyboardContract
) {

    operator fun invoke(): KeyboardSetupModel = KeyboardSetupModel(
        isEnabled = keyboardContract.isKeyboardEnabled(),
        isSelected = keyboardContract.isKeyboardSelected()
    )
}
