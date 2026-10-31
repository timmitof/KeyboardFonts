package kg.timmitof.keyboard.data.language

import android.content.res.Resources
import java.util.Locale

internal actual fun deviceLocale(): DeviceLocale {
    val locale = Resources.getSystem().configuration.locales.takeIf { !it.isEmpty }?.get(0) ?: Locale.getDefault()
    return DeviceLocale(language = locale.language, country = locale.country)
}
