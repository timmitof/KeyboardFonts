package kg.timmitof.feature_settings.presentation.di

import kg.timmitof.feature_settings.presentation.screens.SettingsViewModel
import kg.timmitof.feature_settings.presentation.screens.crop.BackgroundCropViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsPresentationModule = module {

    viewModelOf(::SettingsViewModel)

    viewModelOf(::BackgroundCropViewModel)
}
