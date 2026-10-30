package kg.timmitof.keyboard.integration.di

import kg.timmitof.keyboard.integration.KeyboardContract
import kg.timmitof.keyboard.integration.KeyboardContractImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val keyboardIntegrationModule: Module = module {

    factoryOf(::KeyboardContractImpl) bind KeyboardContract::class
}
