package kg.timmitof.feature_settings.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kg.timmitof.feature_settings.data.interactor.SettingsInteractorImpl
import kg.timmitof.feature_settings.domain.interactor.SettingsInteractor

@Module
@InstallIn(SingletonComponent::class)
abstract class InteractorModule {

    @Binds
    abstract fun bindSettingsInteractor(impl: SettingsInteractorImpl): SettingsInteractor
}
