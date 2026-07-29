package kg.timmitof.feature_home.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kg.timmitof.feature_home.domain.interactor.HomeInteractor
import kg.timmitof.feature_home.domain.repository.ProjectRepository
import kg.timmitof.feature_home.domain.repository.TemplateRepository
import kg.timmitof.feature_home.domain.usecase.GetKeyboardSetupUseCase
import kg.timmitof.feature_home.domain.usecase.GetTemplatesUseCase
import kg.timmitof.feature_home.domain.usecase.GetUserProjectsUseCase
import kg.timmitof.feature_home.domain.usecase.OpenKeyboardSetupUseCase
import kg.timmitof.keyboard.integration.KeyboardContract

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
    fun provideGetKeyboardSetupUseCase(
        keyboardContract: KeyboardContract
    ): GetKeyboardSetupUseCase {
        return GetKeyboardSetupUseCase(keyboardContract)
    }

    @Provides
    fun provideOpenKeyboardSetupUseCase(
        keyboardContract: KeyboardContract
    ): OpenKeyboardSetupUseCase {
        return OpenKeyboardSetupUseCase(keyboardContract)
    }

    @Provides
    fun provideHomeInteractor(
        getTemplatesUseCase: GetTemplatesUseCase,
        getUserProjectsUseCase: GetUserProjectsUseCase,
        getKeyboardSetupUseCase: GetKeyboardSetupUseCase,
        openKeyboardSetupUseCase: OpenKeyboardSetupUseCase
    ): HomeInteractor {
        return HomeInteractor(
            getTemplatesUseCase = getTemplatesUseCase,
            getUserProjectsUseCase = getUserProjectsUseCase,
            getKeyboardSetupUseCase = getKeyboardSetupUseCase,
            openKeyboardSetupUseCase = openKeyboardSetupUseCase
        )
    }
}