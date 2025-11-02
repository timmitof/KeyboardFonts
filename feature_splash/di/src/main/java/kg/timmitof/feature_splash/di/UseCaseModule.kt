package kg.timmitof.feature_splash.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kg.timmitof.feature_splash.domain.interactor.LoadTemplatesInteractor
import kg.timmitof.feature_splash.domain.repository.TemplateRepository
import kg.timmitof.feature_splash.domain.usecase.InitializeTemplatesUseCase

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    fun provideInitializeTemplateUseCase(
        templateRepository: TemplateRepository
    ): InitializeTemplatesUseCase {
        return InitializeTemplatesUseCase(templateRepository)
    }

    @Provides
    fun provideLoadTemplateInteractor(
        initializeTemplatesUseCase: InitializeTemplatesUseCase
    ): LoadTemplatesInteractor {
        return LoadTemplatesInteractor(
            initializeTemplatesUseCase = initializeTemplatesUseCase
        )
    }
}