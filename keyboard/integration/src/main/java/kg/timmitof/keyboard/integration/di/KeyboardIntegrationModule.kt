package kg.timmitof.keyboard.integration.di

import kg.timmitof.keyboard.integration.KeyboardContract
import kg.timmitof.keyboard.integration.KeyboardContractImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val keyboardIntegrationModule = module {

    factoryOf(::KeyboardContractImpl) bind KeyboardContract::class
}
