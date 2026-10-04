package kg.timmitof.feature_home.data.interactor

import kg.timmitof.feature_home.data.mapper.toDomain
import kg.timmitof.feature_home.domain.interactor.HomeInteractor
import kg.timmitof.feature_home.domain.model.KeyboardSetupModel
import kg.timmitof.feature_home.domain.model.KeyboardSetupStep
import kg.timmitof.feature_home.domain.model.TemplateModel
import kg.timmitof.feature_home.domain.model.UserProjectModel
import kg.timmitof.feature_home.domain.repository.ProjectRepository
import kg.timmitof.feature_home.domain.repository.TemplateRepository
import kg.timmitof.keyboard.integration.KeyboardContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HomeInteractorImpl @Inject constructor(
    private val templateRepository: TemplateRepository,
    private val projectRepository: ProjectRepository,
    private val keyboardContract: KeyboardContract,
) : HomeInteractor {

    override suspend fun getAllTemplates(): List<TemplateModel> =
        templateRepository.getAllTemplates()

    override suspend fun getUserProjects(): List<UserProjectModel> =
        projectRepository.getAllUserProjects()

    override fun getKeyboardSetup(): KeyboardSetupModel =
        keyboardContract.getKeyboardState().toDomain()

    override fun observeKeyboardSetup(): Flow<KeyboardSetupModel> =
        keyboardContract.observeKeyboardState().map { it.toDomain() }

    /** Каждому шагу — свой системный экран. */
    override fun openKeyboardSetup(step: KeyboardSetupStep) = when (step) {
        KeyboardSetupStep.ENABLE -> keyboardContract.openKeyboardSettings()
        KeyboardSetupStep.SELECT -> keyboardContract.showKeyboardPicker()
        KeyboardSetupStep.DONE -> Unit
    }
}
