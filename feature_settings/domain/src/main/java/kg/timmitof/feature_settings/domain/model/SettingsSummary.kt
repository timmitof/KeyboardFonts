package kg.timmitof.feature_settings.domain.model

import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.domain.model.KeyboardLayout
import kg.timmitof.keyboard.font.domain.model.KeyboardFont

/**
 * Всё, что экран показывает помимо самих настроек: языки, шрифты, статус клавиатуры
 * и раскладку для предпросмотра.
 *
 * @property languages языковые раскладки — в порядке переключения пробелом.
 * @property selectedLanguage язык, на котором клавиатура откроется.
 * @property fonts шрифты панели над клавишами.
 * @property selectedFont шрифт, которым подписаны клавиши в предпросмотре.
 * @property previewLayout буквенная раскладка выбранного языка для предпросмотра.
 * @property isKeyboardReady клавиатура включена в системе и выбрана текущей.
 */
data class SettingsSummary(
    val languages: List<KeyboardLanguage> = emptyList(),
    val selectedLanguage: KeyboardLanguage? = null,
    val fonts: List<KeyboardFont> = emptyList(),
    val selectedFont: KeyboardFont = KeyboardFont.Default,
    val previewLayout: KeyboardLayout? = null,
    val isKeyboardReady: Boolean = false,
)
