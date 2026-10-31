package kg.timmitof.keyboard.clipboard.data.di

import kg.timmitof.keyboard.clipboard.data.SystemClipboardSource
import kg.timmitof.keyboard.clipboard.data.SystemClipboardSourceImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformClipboardModule: Module = module {

    singleOf(::SystemClipboardSourceImpl) bind SystemClipboardSource::class
}
