package kg.timmitof.feature_home.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kg.timmitof.feature_home.data.repository.ProjectRepositoryImpl
import kg.timmitof.feature_home.data.repository.TemplateRepositoryImpl
import kg.timmitof.feature_home.domain.repository.ProjectRepository
import kg.timmitof.feature_home.domain.repository.TemplateRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindTemplateRepository(impl: TemplateRepositoryImpl): TemplateRepository

    @Binds
    abstract fun bindProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository
}