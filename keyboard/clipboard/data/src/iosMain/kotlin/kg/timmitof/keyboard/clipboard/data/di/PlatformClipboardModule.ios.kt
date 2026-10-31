package kg.timmitof.keyboard.clipboard.data.di

import kg.timmitof.keyboard.clipboard.data.IosSystemClipboardSource
import kg.timmitof.keyboard.clipboard.data.SystemClipboardSource
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformClipboardModule: Module = module {

    singleOf(::IosSystemClipboardSource) bind SystemClipboardSource::class
}
