package kg.timmitof.keyboard.translate.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kg.timmitof.keyboard.translate.data.repository.TranslatorRepositoryImpl
import kg.timmitof.keyboard.translate.domain.repository.TranslatorRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TranslatorDataModule {

    @Binds
    @Singleton
    abstract fun bindTranslatorRepository(impl: TranslatorRepositoryImpl): TranslatorRepository
}
