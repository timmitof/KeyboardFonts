package kg.timmitof.feature_home.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kg.timmitof.feature_home.domain.interactor.HomeInteractor
import kg.timmitof.feature_home.domain.repository.ProjectRepository
import kg.timmitof.feature_home.domain.repository.TemplateRepository
import kg.timmitof.feature_home.domain.usecase.GetTemplatesUseCase
import kg.timmitof.feature_home.domain.usecase.GetUserProjectsUseCase

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    fun provideGetTemplatesUseCase(
        templateRepository: TemplateRepository
    ): GetTemplatesUseCase {
        return GetTemplatesUseCase(templateRepository)
    }

    @Provides
    fun provideGetUserProjectsUseCase(
        projectRepository: ProjectRepository
    ): GetUserProjectsUseCase {
        return GetUserProjectsUseCase(projectRepository)
    }

    @Provides
    fun provideHomeInteractor(
        getTemplatesUseCase: GetTemplatesUseCase,
        getUserProjectsUseCase: GetUserProjectsUseCase
    ): HomeInteractor {
        return HomeInteractor(
            getTemplatesUseCase = getTemplatesUseCase,
            getUserProjectsUseCase = getUserProjectsUseCase
        )
    }
}