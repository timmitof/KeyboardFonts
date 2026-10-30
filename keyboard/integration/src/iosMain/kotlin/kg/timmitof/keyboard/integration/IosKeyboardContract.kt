package kg.timmitof.keyboard.integration

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

/**
 * iOS не сообщает приложению, добавлена ли его клавиатура, и не даёт переключить её программно:
 * состояние всегда «не включена», а настройка — переход в раздел приложения в «Настройках».
 */
class IosKeyboardContract : KeyboardContract {

    override fun getKeyboardState() = KeyboardState()

    override fun observeKeyboardState(): Flow<KeyboardState> = flowOf(getKeyboardState())

    override fun openKeyboardSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
    }

    // Системный выбор клавиатуры на iOS — только кнопка-глобус на самой клавиатуре.
    override fun showKeyboardPicker() = Unit
}
