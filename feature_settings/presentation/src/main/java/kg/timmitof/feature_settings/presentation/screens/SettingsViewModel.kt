package kg.timmitof.feature_settings.presentation.screens

import dagger.hilt.android.lifecycle.HiltViewModel
import kg.timmitof.core.navigation.graphs.HomeGraph
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseViewModel
import kg.timmitof.feature_settings.domain.interactor.SettingsInteractor
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import org.orbitmvi.orbit.syntax.Syntax
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor
) : BaseViewModel<SettingsState, SettingsSideEffect, SettingsEvent>(SettingsState()) {

    init {
        observeSettings()
    }

    override fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ToggleChanged -> setToggle(event.toggle, event.enabled)
            is SettingsEvent.ThemeChanged -> setTheme(event.mode)
            is SettingsEvent.ScreenResumed -> loadSummary()
            is SettingsEvent.CheckKeyboardClicked -> navigateTo(HomeGraph.CheckKeyboardScreen)
        }
    }

    override suspend fun Syntax<SettingsState, BaseSideEffect>.onBootstrap() {
        loadSummary()
    }

    /** Настройки правит и этот экран, и клавиатура — состояние всегда идёт из хранилища. */
    private fun observeSettings() = intent {
        settingsInteractor.observeSettings().collect { settings ->
            reduce { state.copy(settings = settings) }
        }
    }

    private fun setToggle(toggle: KeyboardToggle, enabled: Boolean) = intent {
        settingsInteractor.setToggle(toggle, enabled)
    }

    private fun setTheme(mode: KeyboardThemeMode) = intent {
        settingsInteractor.setTheme(mode)
    }

    private fun loadSummary() = intent {
        val summary = settingsInteractor.getSummary()
        if (summary != state.summary) {
            reduce { state.copy(summary = summary) }
        }
    }
}
