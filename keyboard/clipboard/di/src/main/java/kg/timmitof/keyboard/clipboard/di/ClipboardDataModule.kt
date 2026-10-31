package kg.timmitof.keyboard.clipboard.di

import kg.timmitof.keyboard.clipboard.data.di.platformClipboardModule
import kg.timmitof.keyboard.clipboard.data.repository.ClipboardRepositoryImpl
import kg.timmitof.keyboard.clipboard.domain.repository.ClipboardRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val clipboardDataModule = module {

    includes(platformClipboardModule)

    singleOf(::ClipboardRepositoryImpl) bind ClipboardRepository::class
}
