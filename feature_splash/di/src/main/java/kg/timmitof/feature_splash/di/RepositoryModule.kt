package kg.timmitof.feature_splash.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kg.timmitof.feature_splash.data.repository.TemplateRepositoryImpl
import kg.timmitof.feature_splash.domain.repository.TemplateRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindTemplateRepository(impl: TemplateRepositoryImpl): TemplateRepository
}