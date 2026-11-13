package kg.timmitof.keyboard.translate.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kg.timmitof.keyboard.translate.data.repository.TranslationModelRepositoryImpl
import kg.timmitof.keyboard.translate.data.repository.TranslationRepositoryImpl
import kg.timmitof.keyboard.translate.data.repository.TranslatorPreferencesRepositoryImpl
import kg.timmitof.keyboard.translate.data.source.MlKitTranslatorEngine
import kg.timmitof.keyboard.translate.data.source.TranslatorEngine
import kg.timmitof.keyboard.translate.domain.repository.TranslationModelRepository
import kg.timmitof.keyboard.translate.domain.repository.TranslationRepository
import kg.timmitof.keyboard.translate.domain.repository.TranslatorPreferencesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TranslatorDataModule {

    @Binds
    @Singleton
    abstract fun bindTranslationRepository(impl: TranslationRepositoryImpl): TranslationRepository

    @Binds
    @Singleton
    abstract fun bindTranslationModelRepository(impl: TranslationModelRepositoryImpl): TranslationModelRepository

    @Binds
    @Singleton
    abstract fun bindTranslatorPreferencesRepository(
        impl: TranslatorPreferencesRepositoryImpl,
    ): TranslatorPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindTranslatorEngine(impl: MlKitTranslatorEngine): TranslatorEngine
}
