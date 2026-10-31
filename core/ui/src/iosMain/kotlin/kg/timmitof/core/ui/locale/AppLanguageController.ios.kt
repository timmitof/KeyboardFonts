package kg.timmitof.core.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

@Composable
actual fun rememberAppLanguageController(): AppLanguageController = remember { IosAppLanguageController() }

/** Язык приложения на iOS выбирают в «Настройки › приложение › Язык»; после выбора система перезапускает приложение. */
private class IosAppLanguageController : AppLanguageController {

    override val current: AppLanguage = NSBundle.mainBundle.preferredLocalizations
        .firstOrNull()
        .let { it as? String }
        ?.split('-')
        .let { parts -> resolveAppLanguage(parts?.getOrNull(0), parts?.getOrNull(1)) }

    override val isInAppSelection: Boolean = false

    override fun select(language: AppLanguage) = openSystemSettings()

    override fun openSystemSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
    }
}
