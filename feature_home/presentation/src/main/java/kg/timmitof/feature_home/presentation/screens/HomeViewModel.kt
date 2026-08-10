package kg.timmitof.feature_home.presentation.screens

import dagger.hilt.android.lifecycle.HiltViewModel
import kg.timmitof.core.ui.base.BaseViewModel
import kg.timmitof.feature_home.domain.interactor.HomeInteractor
import kg.timmitof.feature_home.domain.model.KeyboardSetupStep
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeInteractor: HomeInteractor
): BaseViewModel<HomeState, HomeSideEffect, HomeEvent>(HomeState()) {

    init {
        observeKeyboardSetup()
    }

    override fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.BackgroundSelected -> Unit
            is HomeEvent.KeyboardSetupChecked -> checkKeyboardSetup()
            is HomeEvent.SetupStepClicked -> openSetupStep(event.step)
        }
    }

    private fun observeKeyboardSetup() = intent {
        homeInteractor.observeKeyboardSetup().collect { keyboardSetup ->
            reduce { state.copy(keyboardSetup = keyboardSetup) }
        }
    }

    private fun checkKeyboardSetup() = intent {
        val keyboardSetup = homeInteractor.getKeyboardSetup()
        if (keyboardSetup != state.keyboardSetup) {
            reduce { state.copy(keyboardSetup = keyboardSetup) }
        }
    }

    private fun openSetupStep(step: KeyboardSetupStep) = intent {
        homeInteractor.openKeyboardSetup(step)
    }
}
