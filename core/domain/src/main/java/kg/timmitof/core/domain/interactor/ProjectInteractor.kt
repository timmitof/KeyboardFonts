package kg.timmitof.core.domain.interactor

import kg.timmitof.core.domain.model.TemplateModel
import kg.timmitof.core.domain.repository.ProjectRepository
import javax.inject.Inject

/**
 * Методы для работы с фонами
 */
class ProjectInteractor @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend fun getAllTemplates(): List<TemplateModel> = projectRepository.getAllTemplates()
}