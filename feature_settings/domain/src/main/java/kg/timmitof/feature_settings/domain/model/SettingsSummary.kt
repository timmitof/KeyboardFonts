package kg.timmitof.feature_settings.domain.model

import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.domain.model.KeyboardLayout
import kg.timmitof.keyboard.font.domain.model.KeyboardFont

data class SettingsSummary(
    val selectedLanguage: KeyboardLanguage? = null,
    val selectedFont: KeyboardFont = KeyboardFont.Default,
    val previewLayout: KeyboardLayout? = null,
    val isKeyboardReady: Boolean = false,
)
