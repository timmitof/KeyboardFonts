package kg.timmitof.feature_home.presentation.screens

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.timmitof.core.analytics.Analytics
import kg.timmitof.core.analytics.AnalyticsEvents
import kg.timmitof.core.navigation.graphs.HomeGraph
import kg.timmitof.core.navigation.graphs.SettingsGraph
import kg.timmitof.core.ui.base.BaseViewModel
import kg.timmitof.feature_home.domain.interactor.HomeInteractor
import kg.timmitof.feature_home.domain.model.KeyboardSetupModel
import kg.timmitof.feature_home.domain.model.KeyboardSetupStep
import kg.timmitof.core.ui.base.BaseSideEffect
import org.orbitmvi.orbit.syntax.Syntax
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val homeInteractor: HomeInteractor,
    savedStateHandle: SavedStateHandle,
    analytics: Analytics,
) : BaseViewModel<OnboardingState, OnboardingSideEffect, OnboardingEvent>(
    // Статус читается сразу: экран не должен мигнуть первым шагом, когда клавиатура уже подключена.
    OnboardingState(setup = homeInteractor.getKeyboardSetup()),
    analytics,
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
        homeInteractor.observeKeyboardSetup().collect { setup -> applySetup(setup) }
    }

    private fun checkKeyboardSetup() = intent {
        applySetup(homeInteractor.getKeyboardSetup())
    }

    /** Шаги выполняются в системных настройках — их завершение видно только по смене статуса. */
    private suspend fun Syntax<OnboardingState, BaseSideEffect>.applySetup(setup: KeyboardSetupModel) {
        val previous = state.setup
        if (setup == previous) return

        if (setup.isEnabled && !previous.isEnabled) analytics.log(AnalyticsEvents.Onboarding.stepDone(STEP_ENABLE))
        if (setup.isSelected && !previous.isSelected) analytics.log(AnalyticsEvents.Onboarding.stepDone(STEP_SELECT))
        reduce { state.copy(setup = setup) }
    }

    private fun openSetupStep(step: KeyboardSetupStep) = intent {
        analytics.log(AnalyticsEvents.Onboarding.stepClick(step.name.lowercase()))
        homeInteractor.openKeyboardSetup(step)
    }

    /** После пробы — сразу в «Студию», а не на пустой экран «Готово». */
    private fun finish() {
        analytics.log(AnalyticsEvents.Onboarding.finish(isFromSettings))
        if (isFromSettings) {
            navigateBack()
        } else {
            navigateTo(destination = SettingsGraph, popUpTo = HomeGraph, inclusive = true)
        }
    }

    private companion object {
        const val STEP_ENABLE = "enable"
        const val STEP_SELECT = "select"
    }
}
