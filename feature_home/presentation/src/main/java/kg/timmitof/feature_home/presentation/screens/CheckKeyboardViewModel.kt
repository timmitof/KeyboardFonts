package kg.timmitof.feature_home.presentation.screens

import dagger.hilt.android.lifecycle.HiltViewModel
import kg.timmitof.core.ui.base.BaseViewModel
import kg.timmitof.feature_home.domain.interactor.HomeInteractor
import kg.timmitof.feature_home.domain.model.KeyboardSetupStep
import javax.inject.Inject

@HiltViewModel
class CheckKeyboardViewModel @Inject constructor(
    private val homeInteractor: HomeInteractor
) : BaseViewModel<CheckKeyboardState, CheckKeyboardSideEffect, CheckKeyboardEvent>(CheckKeyboardState()) {

    init {
        observeKeyboardSetup()
    }

    override fun onEvent(event: CheckKeyboardEvent) {
        when (event) {
            is CheckKeyboardEvent.KeyboardSetupChecked -> checkKeyboardSetup()
            is CheckKeyboardEvent.SetupStepClicked -> openSetupStep(event.step)
            is CheckKeyboardEvent.BackClicked -> navigateBack()
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
