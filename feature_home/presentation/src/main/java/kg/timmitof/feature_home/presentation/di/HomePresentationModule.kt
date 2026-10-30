package kg.timmitof.feature_home.presentation.di

import kg.timmitof.feature_home.presentation.screens.OnboardingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homePresentationModule = module {

    viewModelOf(::OnboardingViewModel)
}
