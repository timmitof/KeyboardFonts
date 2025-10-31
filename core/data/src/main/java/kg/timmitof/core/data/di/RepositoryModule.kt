package kg.timmitof.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kg.timmitof.core.data.repository.BackgroundRepositoryImpl
import kg.timmitof.core.domain.repository.BackgroundRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindBackgroundRepository(impl: BackgroundRepositoryImpl): BackgroundRepository
}