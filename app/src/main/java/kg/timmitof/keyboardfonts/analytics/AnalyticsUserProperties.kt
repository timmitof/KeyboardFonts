package kg.timmitof.keyboardfonts.analytics

import kg.timmitof.core.analytics.Analytics
import kg.timmitof.core.analytics.UserProperties
import kg.timmitof.keyboard.domain.model.KeyboardBackground
import kg.timmitof.keyboard.domain.model.KeyboardLanguages
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.repository.KeyboardSettingsRepository
import kg.timmitof.keyboard.domain.repository.LanguageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Свойства пользователя повторяют текущие настройки клавиатуры: по ним в Firebase видно, какая доля
 * пользователей включила звук, фон или подсказки. Настройки правят и приложение, и клавиатура — следим за хранилищем.
 */
@Singleton
internal class AnalyticsUserProperties @Inject constructor(
    private val analytics: Analytics,
    private val settingsRepository: KeyboardSettingsRepository,
    private val languageRepository: LanguageRepository,
) {

    fun start(scope: CoroutineScope) {
        settingsRepository.observeSettings()
            .map { it.toProperties() }
            .distinctUntilChanged()
            .onEach(::apply)
            .launchIn(scope)

        languageRepository.observeLanguages()
            .map { it.toProperties() }
            .distinctUntilChanged()
            .onEach(::apply)
            .launchIn(scope)
    }

    private fun apply(properties: Map<String, String>) {
        properties.forEach { (name, value) -> analytics.setUserProperty(name, value) }
    }

    private fun KeyboardSettings.toProperties(): Map<String, String> = mapOf(
        UserProperties.THEME to theme.key,
        UserProperties.HEIGHT to height.key,
        UserProperties.BACKGROUND to background.analyticsType,
        UserProperties.KEY_COLORS to if ((keyColor ?: specialKeyColor ?: enterColor) != null) "custom" else "auto",
        UserProperties.SOUND to if (isSoundEnabled) soundPack.key else "off",
        UserProperties.VIBRATION to isVibrationEnabled.toString(),
        UserProperties.SUGGESTIONS to isSuggestionsEnabled.toString(),
        UserProperties.AUTO_CORRECT to isAutoCorrectEnabled.toString(),
        UserProperties.LEARNING to isLearningEnabled.toString(),
        UserProperties.FONTS_PANEL to isFontsPanelEnabled.toString(),
        UserProperties.DIGITS_ROW to isDigitsRowEnabled.toString(),
        UserProperties.KEY_PREVIEW to isKeyPreviewEnabled.toString(),
        UserProperties.KEY_OUTLINE to isKeyOutlineEnabled.toString(),
    )

    private fun KeyboardLanguages.toProperties(): Map<String, String> = mapOf(
        UserProperties.LANGUAGES to enabled.joinToString(",") { it.code },
        UserProperties.LANGUAGE_COUNT to enabled.size.toString(),
        UserProperties.MAIN_LANGUAGE to selected.code,
    )

    private val KeyboardBackground.analyticsType: String
        get() = when (this) {
            KeyboardBackground.None -> "none"
            is KeyboardBackground.Solid -> "solid"
            is KeyboardBackground.Pattern -> "pattern_${pattern.key}"
            is KeyboardBackground.Photo -> "photo"
        }
}
