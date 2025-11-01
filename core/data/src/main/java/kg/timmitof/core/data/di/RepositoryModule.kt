package kg.timmitof.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kg.timmitof.core.data.repository.ProjectRepositoryImpl
import kg.timmitof.core.domain.repository.ProjectRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository
}