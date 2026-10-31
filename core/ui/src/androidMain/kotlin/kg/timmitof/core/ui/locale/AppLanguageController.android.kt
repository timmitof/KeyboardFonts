package kg.timmitof.core.ui.locale

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberAppLanguageController(): AppLanguageController {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(context, configuration) { AndroidAppLanguageController(context) }
}

private class AndroidAppLanguageController(private val context: Context) : AppLanguageController {

    override val current: AppLanguage = AppLocale.current(context)

    override val isInAppSelection: Boolean = true

    override fun select(language: AppLanguage) {
        context.findActivity()?.let { AppLocale.select(it, language) }
    }

    /** С Android 13 у приложения есть свой экран языка в системных настройках. */
    override fun openSystemSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS, Uri.fromParts("package", context.packageName, null))
        runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
