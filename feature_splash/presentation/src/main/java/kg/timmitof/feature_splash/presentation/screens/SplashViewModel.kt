package kg.timmitof.feature_splash.presentation.screens

import dagger.hilt.android.lifecycle.HiltViewModel
import kg.timmitof.core.navigation.graphs.HomeGraph
import kg.timmitof.core.navigation.graphs.SettingsGraph
import kg.timmitof.core.navigation.graphs.SplashGraph
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseViewModel
import kg.timmitof.feature_splash.domain.interactor.SplashInteractor
import org.orbitmvi.orbit.syntax.Syntax
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val splashInteractor: SplashInteractor
): BaseViewModel<SplashState, SplashSideEffect, SplashEvent>(SplashState()) {
    override fun onEvent(event: SplashEvent) {
        when (event) {
            SplashEvent.AnimationFinished -> navigateTo(
                destination = if (splashInteractor.isKeyboardReady()) SettingsGraph else HomeGraph,
                popUpTo = SplashGraph,
                inclusive = true
            )
        }
    }

    override suspend fun Syntax<SplashState, BaseSideEffect>.onBootstrap() {
        reduce { state.copy(isLoading = false) }
    }
}
