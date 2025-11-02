package kg.timmitof.feature_home.domain.usecase

import kg.timmitof.feature_home.domain.model.TemplateModel
import kg.timmitof.feature_home.domain.repository.TemplateRepository

class GetTemplatesUseCase(
    private val templateRepository: TemplateRepository
) {

    suspend operator fun invoke(): List<TemplateModel> {
        return templateRepository.getAllTemplates()
    }
}