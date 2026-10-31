package kg.timmitof.keyboard.data.language

/** Язык и регион устройства, а не приложения: у приложения может стоять свой язык интерфейса. */
internal data class DeviceLocale(val language: String, val country: String)

internal expect fun deviceLocale(): DeviceLocale
