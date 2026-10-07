package kg.timmitof.keyboard.suggestion.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kg.timmitof.keyboard.suggestion.data.repository.SuggestionRepositoryImpl
import kg.timmitof.keyboard.suggestion.domain.repository.SuggestionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SuggestionDataModule {

    @Binds
    @Singleton
    abstract fun bindSuggestionRepository(impl: SuggestionRepositoryImpl): SuggestionRepository
}
