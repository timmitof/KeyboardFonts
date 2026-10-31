package kg.timmitof.keyboard.di

import kg.timmitof.keyboard.data.AssetJsonKeyboardLayoutLoader
import kg.timmitof.keyboard.data.JsonKeyboardLayoutLoader
import kg.timmitof.keyboard.data.di.platformKeyboardDataModule
import kg.timmitof.keyboard.data.emoji.EmojiPreferences
import kg.timmitof.keyboard.data.emoji.EmojiSearchIndex
import kg.timmitof.keyboard.data.emoji.PreferredEmojiVariantDataSource
import kg.timmitof.keyboard.data.emoji.RecentEmojiDataSource
import kg.timmitof.keyboard.data.language.KeyboardPreferences
import kg.timmitof.keyboard.data.language.LanguageCatalogDataSource
import kg.timmitof.keyboard.data.language.LanguagePreferencesDataSource
import kg.timmitof.keyboard.data.repository.BackgroundPhotoRepositoryImpl
import kg.timmitof.keyboard.data.repository.EmojiRepositoryImpl
import kg.timmitof.keyboard.data.repository.KeyboardLayoutRepositoryImpl
import kg.timmitof.keyboard.data.repository.KeyboardSettingsRepositoryImpl
import kg.timmitof.keyboard.data.repository.LanguageRepositoryImpl
import kg.timmitof.keyboard.data.settings.KeyboardSettingsDataSource
import kg.timmitof.keyboard.domain.repository.BackgroundPhotoRepository
import kg.timmitof.keyboard.domain.repository.EmojiRepository
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import kg.timmitof.keyboard.domain.repository.KeyboardSettingsRepository
import kg.timmitof.keyboard.domain.repository.LanguageRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val keyboardDataModule = module {

    includes(platformKeyboardDataModule)

    // DataStore допускает одно хранилище на файл в процессе — только single.
    singleOf(::KeyboardPreferences)
    singleOf(::EmojiPreferences)

    singleOf(::AssetJsonKeyboardLayoutLoader) bind JsonKeyboardLayoutLoader::class

    singleOf(::KeyboardLayoutRepositoryImpl) bind KeyboardLayoutRepository::class

    singleOf(::EmojiRepositoryImpl) bind EmojiRepository::class

    singleOf(::LanguageRepositoryImpl) bind LanguageRepository::class

    singleOf(::KeyboardSettingsRepositoryImpl) bind KeyboardSettingsRepository::class

    singleOf(::BackgroundPhotoRepositoryImpl) bind BackgroundPhotoRepository::class

    singleOf(::RecentEmojiDataSource)
    singleOf(::PreferredEmojiVariantDataSource)
    singleOf(::EmojiSearchIndex)
    singleOf(::LanguageCatalogDataSource)
    singleOf(::LanguagePreferencesDataSource)
    singleOf(::KeyboardSettingsDataSource)
}
