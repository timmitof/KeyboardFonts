package kg.timmitof.feature_home.domain.usecase

import kg.timmitof.feature_home.domain.model.KeyboardSetupModel
import kg.timmitof.keyboard.integration.KeyboardContract
import kg.timmitof.keyboard.integration.KeyboardState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Состояние подключения клавиатуры: разово и потоком. */
class GetKeyboardSetupUseCase(
    private val keyboardContract: KeyboardContract
) {

    operator fun invoke(): KeyboardSetupModel = keyboardContract.getKeyboardState().toModel()

    fun observe(): Flow<KeyboardSetupModel> =
        keyboardContract.observeKeyboardState().map { it.toModel() }

    private fun KeyboardState.toModel() = KeyboardSetupModel(
        isEnabled = isEnabled,
        isSelected = isSelected
    )
}
