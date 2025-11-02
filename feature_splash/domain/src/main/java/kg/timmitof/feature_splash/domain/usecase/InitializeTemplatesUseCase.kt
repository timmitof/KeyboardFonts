package kg.timmitof.feature_splash.domain.usecase

import kg.timmitof.feature_splash.domain.repository.TemplateRepository

class InitializeTemplatesUseCase(
    private val repository: TemplateRepository
) {
    suspend operator fun invoke() {
        if (repository.getTemplateCount() != 0) return
        repository.loadTemplatesFromAsset()
    }
}