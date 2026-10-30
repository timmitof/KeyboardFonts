package kg.timmitof.keyboard.suggestion.di

import kg.timmitof.keyboard.suggestion.data.LanguageModelLoader
import kg.timmitof.keyboard.suggestion.data.SuggestionEngine
import kg.timmitof.keyboard.suggestion.data.UserDictionaryStore
import kg.timmitof.keyboard.suggestion.data.repository.SuggestionRepositoryImpl
import kg.timmitof.keyboard.suggestion.domain.repository.SuggestionRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val suggestionDataModule = module {

    singleOf(::SuggestionRepositoryImpl) bind SuggestionRepository::class

    singleOf(::LanguageModelLoader)
    singleOf(::UserDictionaryStore)
    singleOf(::SuggestionEngine)
}
