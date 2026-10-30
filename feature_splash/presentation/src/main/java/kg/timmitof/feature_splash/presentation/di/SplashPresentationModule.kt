package kg.timmitof.feature_splash.presentation.di

import kg.timmitof.feature_splash.presentation.screens.SplashViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val splashPresentationModule = module {

    viewModelOf(::SplashViewModel)
}
