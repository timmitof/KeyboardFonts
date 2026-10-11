package kg.timmitof.feature_settings.presentation.screens

import dagger.hilt.android.lifecycle.HiltViewModel
import kg.timmitof.core.analytics.Analytics
import kg.timmitof.core.analytics.AnalyticsEvents
import kg.timmitof.core.navigation.graphs.HomeGraph
import kg.timmitof.core.navigation.graphs.SettingsGraph
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseViewModel
import kg.timmitof.feature_settings.domain.interactor.SettingsInteractor
import kg.timmitof.feature_settings.presentation.studio.StudioTab
import kg.timmitof.keyboard.domain.model.KeyColorTarget
import kg.timmitof.keyboard.domain.model.KeyboardBackground
import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardSoundPack
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import org.orbitmvi.orbit.syntax.Syntax
import kotlin.math.roundToInt
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor,
    analytics: Analytics,
) : BaseViewModel<SettingsState, SettingsSideEffect, SettingsEvent>(SettingsState(), analytics) {

    init {
        observeSettings()
        observeFontPanel()
        observeLanguages()
        observePhotos()
        observeClipboard()
    }

    override fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ToggleChanged -> setToggle(event.toggle, event.enabled)
            is SettingsEvent.ThemeChanged -> setTheme(event.mode)
            is SettingsEvent.HeightChanged -> setHeight(event.height)
            is SettingsEvent.KeyColorChanged -> setKeyColor(event.target, event.argb)
            is SettingsEvent.BackgroundSelected -> setBackground(event.background)
            is SettingsEvent.BackgroundPhotoPicked -> addPhoto(event.uri)
            is SettingsEvent.EditPhotoClicked -> navigateTo(SettingsGraph.BackgroundCropScreen(event.photoId))
            is SettingsEvent.SoundPackChanged -> setSoundPack(event.pack)
            is SettingsEvent.SoundVolumeChanged -> setSoundVolume(event.volume)
            is SettingsEvent.PanelFontsChanged -> setPanelFonts(event.ids)
            is SettingsEvent.ResetFontPanelClicked -> resetFontPanel()
            is SettingsEvent.EnabledLanguagesChanged -> setEnabledLanguages(event.codes)
            is SettingsEvent.TabSelected -> selectTab(event.tab)
            is SettingsEvent.ClearRecentClipboardClicked -> clearRecentClipboard()
            is SettingsEvent.ScreenResumed -> loadSummary()
            is SettingsEvent.ConnectKeyboardClicked -> {
                analytics.log(AnalyticsEvents.Settings.connectKeyboardClick())
                navigateTo(HomeGraph.OnboardingScreen(isFromSettings = true))
            }
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

    /** Удалили выбранный язык — превью клавиатуры показывает новый, поэтому сводка перечитывается. */
    private fun observeLanguages() = intent {
        settingsInteractor.observeLanguages().collect { languages ->
            reduce { state.copy(languages = languages) }
            val shown = state.summary.selectedLanguage ?: return@collect
            if (languages.selected.code != shown.code) loadSummary()
        }
    }

    private fun setEnabledLanguages(codes: List<String>) = intent {
        if (codes.isEmpty()) return@intent

        settingsInteractor.setEnabledLanguages(codes)
        analytics.log(AnalyticsEvents.Settings.languages(codes))
    }

    private fun setPanelFonts(ids: List<String>) = intent {
        settingsInteractor.setPanelFonts(ids)
        analytics.log(AnalyticsEvents.Settings.fontPanel(ids.size))
    }

    private fun resetFontPanel() = intent {
        settingsInteractor.resetFontPanel()
        analytics.log(AnalyticsEvents.Settings.fontPanelReset())
    }

    private fun setToggle(toggle: KeyboardToggle, enabled: Boolean) = intent {
        settingsInteractor.setToggle(toggle, enabled)
        analytics.log(AnalyticsEvents.Settings.toggle(toggle.key, enabled))
    }

    private fun setTheme(mode: KeyboardThemeMode) = intent {
        settingsInteractor.setTheme(mode)
        analytics.log(AnalyticsEvents.Settings.theme(mode.key))
    }

    private fun setHeight(height: KeyboardHeight) = intent {
        settingsInteractor.setHeight(height)
        analytics.log(AnalyticsEvents.Settings.height(height.key))
    }

    private fun setKeyColor(target: KeyColorTarget, argb: Long?) = intent {
        settingsInteractor.setKeyColor(target, argb)
        analytics.log(AnalyticsEvents.Settings.keyColor(target.name.lowercase(), isCustom = argb != null))
    }

    private fun observePhotos() = intent {
        settingsInteractor.observePhotos().collect { photos ->
            reduce { state.copy(photos = photos) }
        }
    }

    /** Фото копируется к нам целиком и сразу открывается настройка, какая его часть будет видна. */
    private fun addPhoto(uri: String) = intent {
        runCatching { settingsInteractor.addPhoto(uri) }
            .onFailure(analytics::recordError)
            .onSuccess { photo ->
                analytics.log(AnalyticsEvents.Settings.photoAdd())
                navigateTo(SettingsGraph.BackgroundCropScreen(photo.id))
            }
    }

    private fun setBackground(background: KeyboardBackground) = intent {
        settingsInteractor.setBackground(background)
        analytics.log(background.toAnalyticsEvent())
    }

    private fun setSoundPack(pack: KeyboardSoundPack) = intent {
        settingsInteractor.setSoundPack(pack)
        analytics.log(AnalyticsEvents.Settings.soundPack(pack.key))
    }

    private fun setSoundVolume(volume: Float) = intent {
        settingsInteractor.setSoundVolume(volume)
        analytics.log(AnalyticsEvents.Settings.soundVolume((volume * 10).roundToInt() * 10))
    }

    private fun selectTab(tab: StudioTab) = intent {
        if (tab == state.selectedTab) return@intent

        reduce { state.copy(selectedTab = tab) }
        analytics.log(AnalyticsEvents.Settings.tabOpen(tab.name.lowercase()))
    }

    private fun clearRecentClipboard() = intent {
        settingsInteractor.clearRecentClipboard()
        analytics.log(AnalyticsEvents.Settings.clipboardClear())
    }

    private fun loadSummary() = intent {
        val summary = settingsInteractor.getSummary()
        if (summary != state.summary) {
            reduce { state.copy(summary = summary) }
        }
    }

    private fun KeyboardBackground.toAnalyticsEvent() = when (this) {
        KeyboardBackground.None -> AnalyticsEvents.Settings.background(type = "none", value = "")
        is KeyboardBackground.Solid -> AnalyticsEvents.Settings.background(type = "solid", value = "")
        is KeyboardBackground.Pattern -> AnalyticsEvents.Settings.background(type = "pattern", value = pattern.key)
        is KeyboardBackground.Photo -> AnalyticsEvents.Settings.background(type = "photo", value = "")
    }
}
