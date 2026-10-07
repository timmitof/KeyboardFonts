package kg.timmitof.feature_home.data.interactor

import kg.timmitof.feature_home.data.mapper.toDomain
import kg.timmitof.feature_home.domain.interactor.HomeInteractor
import kg.timmitof.feature_home.domain.model.KeyboardSetupModel
import kg.timmitof.feature_home.domain.model.KeyboardSetupStep
import kg.timmitof.keyboard.integration.KeyboardContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HomeInteractorImpl @Inject constructor(
    private val keyboardContract: KeyboardContract,
) : HomeInteractor {

    override fun getKeyboardSetup(): KeyboardSetupModel =
        keyboardContract.getKeyboardState().toDomain()

    override fun observeKeyboardSetup(): Flow<KeyboardSetupModel> =
        keyboardContract.observeKeyboardState().map { it.toDomain() }

    override fun openKeyboardSetup(step: KeyboardSetupStep) = when (step) {
        KeyboardSetupStep.ENABLE -> keyboardContract.openKeyboardSettings()
        KeyboardSetupStep.SELECT -> keyboardContract.showKeyboardPicker()
        KeyboardSetupStep.DONE -> Unit
    }
}
