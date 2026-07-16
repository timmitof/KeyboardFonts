package kg.timmitof.keyboard.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kg.timmitof.keyboard.data.AssetJsonKeyboardLayoutLoader
import kg.timmitof.keyboard.data.JsonKeyboardLayoutLoader
import kg.timmitof.keyboard.data.repository.EmojiRepositoryImpl
import kg.timmitof.keyboard.data.repository.KeyboardLayoutRepositoryImpl
import kg.timmitof.keyboard.data.repository.LanguageRepositoryImpl
import kg.timmitof.keyboard.domain.repository.EmojiRepository
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import kg.timmitof.keyboard.domain.repository.LanguageRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class KeyboardDataModule {

    @Binds
    @Singleton
    abstract fun bindJsonKeyboardLayoutLoader(impl: AssetJsonKeyboardLayoutLoader): JsonKeyboardLayoutLoader

    @Binds
    @Singleton
    abstract fun bindKeyboardLayoutRepository(impl: KeyboardLayoutRepositoryImpl): KeyboardLayoutRepository

    @Binds
    @Singleton
    abstract fun bindEmojiRepository(impl: EmojiRepositoryImpl): EmojiRepository

    @Binds
    @Singleton
    abstract fun bindLanguageRepository(impl: LanguageRepositoryImpl): LanguageRepository
}