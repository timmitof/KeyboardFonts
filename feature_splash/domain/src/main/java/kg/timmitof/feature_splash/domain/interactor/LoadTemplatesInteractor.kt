package kg.timmitof.feature_splash.domain.interactor

import kg.timmitof.feature_splash.domain.usecase.InitializeTemplatesUseCase

class LoadTemplatesInteractor(
    private val initializeTemplatesUseCase: InitializeTemplatesUseCase
) {
    suspend fun invoke() {
        initializeTemplatesUseCase.invoke()
    }
}