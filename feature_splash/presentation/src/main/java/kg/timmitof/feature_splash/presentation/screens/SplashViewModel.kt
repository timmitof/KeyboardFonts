package kg.timmitof.feature_splash.presentation.screens

import dagger.hilt.android.lifecycle.HiltViewModel
import kg.timmitof.core.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(): BaseViewModel<SplashState, SplashSideEffect, SplashEvent>(SplashState()) {
    override fun onEvent(event: SplashEvent) {}
}