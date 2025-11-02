package kg.timmitof.feature_home.domain.interactor

import kg.timmitof.feature_home.domain.usecase.GetTemplatesUseCase
import kg.timmitof.feature_home.domain.usecase.GetUserProjectsUseCase

class HomeInteractor(
    private val getTemplatesUseCase: GetTemplatesUseCase,
    private val getUserProjectsUseCase: GetUserProjectsUseCase
) {

    suspend fun getAllTemplates() = getTemplatesUseCase.invoke()

    suspend fun getUserProjects() = getUserProjectsUseCase.invoke()
}