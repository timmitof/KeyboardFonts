package kg.timmitof.feature_settings.presentation.screens

import dagger.hilt.android.lifecycle.HiltViewModel
import kg.timmitof.core.navigation.graphs.HomeGraph
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseViewModel
import kg.timmitof.feature_settings.domain.interactor.SettingsInteractor
import kg.timmitof.feature_settings.presentation.studio.StudioTab
import kg.timmitof.keyboard.domain.model.KeyboardBackground
import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardSoundPack
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
        observeFontPanel()
        observeClipboard()
    }

    override fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ToggleChanged -> setToggle(event.toggle, event.enabled)
            is SettingsEvent.ThemeChanged -> setTheme(event.mode)
            is SettingsEvent.HeightChanged -> setHeight(event.height)
            is SettingsEvent.EnterColorChanged -> setEnterColor(event.argb)
            is SettingsEvent.BackgroundSelected -> setBackground(event.background)
            is SettingsEvent.BackgroundPhotoPicked -> importBackgroundPhoto(event.uri)
            is SettingsEvent.BackgroundColorClicked -> openBackgroundColor()
            is SettingsEvent.BackgroundDraftChanged -> changeBackgroundDraft(event.argb)
            is SettingsEvent.BackgroundDraftApplied -> applyBackgroundDraft()
            is SettingsEvent.BackgroundDraftDismissed -> dismissBackgroundDraft()
            is SettingsEvent.SoundPackChanged -> setSoundPack(event.pack)
            is SettingsEvent.SoundVolumeChanged -> setSoundVolume(event.volume)
            is SettingsEvent.PanelFontsChanged -> setPanelFonts(event.ids)
            is SettingsEvent.ResetFontPanelClicked -> resetFontPanel()
            is SettingsEvent.TabSelected -> selectTab(event.tab)
            is SettingsEvent.ClearRecentClipboardClicked -> clearRecentClipboard()
            is SettingsEvent.ScreenResumed -> loadSummary()
            is SettingsEvent.ConnectKeyboardClicked -> navigateTo(HomeGraph.OnboardingScreen(isFromSettings = true))
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

    /** Записи в буфер добавляет клавиатура — вкладка видит их сразу. */
    private fun observeClipboard() = intent {
        settingsInteractor.observeClipboard().collect { board ->
            reduce { state.copy(clipboard = board) }
        }
    }

    private fun observeFontPanel() = intent {
        settingsInteractor.observeFontPanel().collect { panel ->
            reduce { state.copy(fontPanel = panel) }
        }
    }

    private fun setPanelFonts(ids: List<String>) = intent {
        settingsInteractor.setPanelFonts(ids)
    }

    private fun resetFontPanel() = intent {
        settingsInteractor.resetFontPanel()
    }

    private fun setToggle(toggle: KeyboardToggle, enabled: Boolean) = intent {
        settingsInteractor.setToggle(toggle, enabled)
    }

    private fun setTheme(mode: KeyboardThemeMode) = intent {
        settingsInteractor.setTheme(mode)
    }

    private fun setHeight(height: KeyboardHeight) = intent {
        settingsInteractor.setHeight(height)
    }

    private fun setEnterColor(argb: Long?) = intent {
        settingsInteractor.setEnterColor(argb)
    }

    private fun setBackground(background: KeyboardBackground) = intent {
        settingsInteractor.setBackground(background)
    }

    private fun importBackgroundPhoto(uri: String) = intent {
        runCatching { settingsInteractor.importBackgroundPhoto(uri) }
    }

    /** Шторка открывается с текущим цветом фона, а если фон не цветной — с первым из готовых. */
    private fun openBackgroundColor() = intent {
        val current = state.settings.background as? KeyboardBackground.Solid
        reduce { state.copy(backgroundDraft = current ?: KeyboardBackground.Solid(DEFAULT_BACKGROUND_COLOR)) }
    }

    private fun changeBackgroundDraft(argb: Long) = intent {
        if (state.backgroundDraft != null) {
            reduce { state.copy(backgroundDraft = KeyboardBackground.Solid(argb)) }
        }
    }

    private fun applyBackgroundDraft() = intent {
        val draft = state.backgroundDraft ?: return@intent
        settingsInteractor.setBackground(draft)
        reduce { state.copy(backgroundDraft = null) }
    }

    private fun dismissBackgroundDraft() = intent {
        reduce { state.copy(backgroundDraft = null) }
    }

    private fun setSoundPack(pack: KeyboardSoundPack) = intent {
        settingsInteractor.setSoundPack(pack)
    }

    private fun setSoundVolume(volume: Float) = intent {
        settingsInteractor.setSoundVolume(volume)
    }

    private fun selectTab(tab: StudioTab) = intent {
        if (tab != state.selectedTab) reduce { state.copy(selectedTab = tab) }
    }

    private fun clearRecentClipboard() = intent {
        settingsInteractor.clearRecentClipboard()
    }

    private fun loadSummary() = intent {
        val summary = settingsInteractor.getSummary()
        if (summary != state.summary) {
            reduce { state.copy(summary = summary) }
        }
    }

    private companion object {
        const val DEFAULT_BACKGROUND_COLOR = 0xFFCDEFE7L
    }
}
