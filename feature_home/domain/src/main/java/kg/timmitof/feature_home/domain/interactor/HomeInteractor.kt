package kg.timmitof.feature_home.domain.interactor

import kg.timmitof.feature_home.domain.model.KeyboardSetupModel
import kg.timmitof.feature_home.domain.model.KeyboardSetupStep
import kg.timmitof.feature_home.domain.model.TemplateModel
import kg.timmitof.feature_home.domain.model.UserProjectModel
import kotlinx.coroutines.flow.Flow

interface HomeInteractor {

    suspend fun getAllTemplates(): List<TemplateModel>

    suspend fun getUserProjects(): List<UserProjectModel>

    /** Текущее состояние подключения клавиатуры. */
    fun getKeyboardSetup(): KeyboardSetupModel

    /** Состояние подключения - обновляется при изменении системных настроек. */
    fun observeKeyboardSetup(): Flow<KeyboardSetupModel>

    /** Открыть системный экран для прохождения шага [step]. */
    fun openKeyboardSetup(step: KeyboardSetupStep)
}
