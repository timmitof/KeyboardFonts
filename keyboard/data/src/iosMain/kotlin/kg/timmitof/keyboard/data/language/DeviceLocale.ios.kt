package kg.timmitof.keyboard.data.language

import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.preferredLanguages

/** Первый язык из настроек устройства; `currentLocale` отдаёт язык приложения. */
internal actual fun deviceLocale(): DeviceLocale {
    val identifier = NSLocale.preferredLanguages.firstOrNull() as? String
    val locale = identifier?.let { NSLocale(localeIdentifier = it) } ?: NSLocale.currentLocale
    return DeviceLocale(language = locale.languageCode, country = locale.countryCode.orEmpty())
}
