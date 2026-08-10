package kg.timmitof.feature_home.domain.interactor

import kg.timmitof.feature_home.domain.model.KeyboardSetupModel
import kg.timmitof.feature_home.domain.model.KeyboardSetupStep
import kg.timmitof.feature_home.domain.usecase.GetKeyboardSetupUseCase
import kg.timmitof.feature_home.domain.usecase.GetTemplatesUseCase
import kg.timmitof.feature_home.domain.usecase.GetUserProjectsUseCase
import kg.timmitof.feature_home.domain.usecase.OpenKeyboardSetupUseCase
import kotlinx.coroutines.flow.Flow

class HomeInteractor(
    private val getTemplatesUseCase: GetTemplatesUseCase,
    private val getUserProjectsUseCase: GetUserProjectsUseCase,
    private val getKeyboardSetupUseCase: GetKeyboardSetupUseCase,
    private val openKeyboardSetupUseCase: OpenKeyboardSetupUseCase
) {

    suspend fun getAllTemplates() = getTemplatesUseCase.invoke()

    suspend fun getUserProjects() = getUserProjectsUseCase.invoke()

    /** Текущее состояние подключения клавиатуры. */
    fun getKeyboardSetup(): KeyboardSetupModel = getKeyboardSetupUseCase.invoke()

    /** Состояние подключения - обновляется при изменении системных настроек. */
    fun observeKeyboardSetup(): Flow<KeyboardSetupModel> = getKeyboardSetupUseCase.observe()

    /** Открыть системный экран для прохождения шага [step]. */
    fun openKeyboardSetup(step: KeyboardSetupStep) = openKeyboardSetupUseCase.invoke(step)
}
