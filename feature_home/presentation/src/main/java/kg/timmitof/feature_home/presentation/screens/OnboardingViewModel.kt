package kg.timmitof.feature_home.presentation.screens

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.timmitof.core.navigation.graphs.HomeGraph
import kg.timmitof.core.navigation.graphs.SettingsGraph
import kg.timmitof.core.ui.base.BaseViewModel
import kg.timmitof.feature_home.domain.interactor.HomeInteractor
import kg.timmitof.feature_home.domain.model.KeyboardSetupStep
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val homeInteractor: HomeInteractor,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<OnboardingState, OnboardingSideEffect, OnboardingEvent>(
    // Статус читается сразу: экран не должен мигнуть первым шагом, когда клавиатура уже подключена.
    OnboardingState(setup = homeInteractor.getKeyboardSetup())
) {

    private val isFromSettings = savedStateHandle.toRoute<HomeGraph.OnboardingScreen>().isFromSettings

    init {
        observeKeyboardSetup()
    }

    override fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.ScreenResumed -> checkKeyboardSetup()
            is OnboardingEvent.StepActionClicked -> openSetupStep(event.step)
            is OnboardingEvent.DoneClicked -> finish()
        }
    }

    private fun observeKeyboardSetup() = intent {
        homeInteractor.observeKeyboardSetup().collect { setup ->
            reduce { state.copy(setup = setup) }
        }
    }

    private fun checkKeyboardSetup() = intent {
        val setup = homeInteractor.getKeyboardSetup()
        if (setup != state.setup) {
            reduce { state.copy(setup = setup) }
        }
    }

    private fun openSetupStep(step: KeyboardSetupStep) = intent {
        homeInteractor.openKeyboardSetup(step)
    }

    /** После пробы — сразу в «Студию», а не на пустой экран «Готово». */
    private fun finish() {
        if (isFromSettings) {
            navigateBack()
        } else {
            navigateTo(destination = SettingsGraph, popUpTo = HomeGraph, inclusive = true)
        }
    }
}
