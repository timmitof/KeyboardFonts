package kg.timmitof.feature_home.domain.usecase

import kg.timmitof.feature_home.domain.model.UserProjectModel
import kg.timmitof.feature_home.domain.repository.ProjectRepository

class GetUserProjectsUseCase(
    private val projectRepository: ProjectRepository
) {

    suspend operator fun invoke(): List<UserProjectModel> {
        return projectRepository.getAllUserProjects()
    }
}