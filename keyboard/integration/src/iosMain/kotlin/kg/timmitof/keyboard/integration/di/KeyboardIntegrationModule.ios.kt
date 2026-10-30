package kg.timmitof.keyboard.integration.di

import kg.timmitof.keyboard.integration.IosKeyboardContract
import kg.timmitof.keyboard.integration.KeyboardContract
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val keyboardIntegrationModule: Module = module {

    factoryOf(::IosKeyboardContract) bind KeyboardContract::class
}
