package kg.timmitof.feature_settings.di

import kg.timmitof.feature_settings.data.interactor.SettingsInteractorImpl
import kg.timmitof.feature_settings.domain.interactor.SettingsInteractor
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsInteractorModule = module {

    factoryOf(::SettingsInteractorImpl) bind SettingsInteractor::class
}
