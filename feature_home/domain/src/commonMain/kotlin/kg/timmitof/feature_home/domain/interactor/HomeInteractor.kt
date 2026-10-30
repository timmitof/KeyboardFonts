package kg.timmitof.feature_home.domain.interactor

import kg.timmitof.feature_home.domain.model.KeyboardSetupModel
import kg.timmitof.feature_home.domain.model.KeyboardSetupStep
import kotlinx.coroutines.flow.Flow

interface HomeInteractor {

    fun getKeyboardSetup(): KeyboardSetupModel

    fun observeKeyboardSetup(): Flow<KeyboardSetupModel>

    fun openKeyboardSetup(step: KeyboardSetupStep)
}
