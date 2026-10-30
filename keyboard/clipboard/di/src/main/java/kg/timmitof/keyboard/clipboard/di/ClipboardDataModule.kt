package kg.timmitof.keyboard.clipboard.di

import kg.timmitof.keyboard.clipboard.data.SystemClipboardSource
import kg.timmitof.keyboard.clipboard.data.repository.ClipboardRepositoryImpl
import kg.timmitof.keyboard.clipboard.domain.repository.ClipboardRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val clipboardDataModule = module {

    singleOf(::ClipboardRepositoryImpl) bind ClipboardRepository::class

    singleOf(::SystemClipboardSource)
}
