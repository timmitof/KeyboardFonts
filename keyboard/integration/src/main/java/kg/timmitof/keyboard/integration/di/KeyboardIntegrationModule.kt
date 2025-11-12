package kg.timmitof.keyboard.integration.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kg.timmitof.keyboard.integration.KeyboardContract
import kg.timmitof.keyboard.integration.KeyboardContractImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class KeyboardIntegrationModule {

    @Binds
    abstract fun provideKeyboardContract(impl: KeyboardContractImpl): KeyboardContract
}