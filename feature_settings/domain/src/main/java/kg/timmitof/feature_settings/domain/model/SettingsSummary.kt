package kg.timmitof.feature_settings.domain.model

/**
 * Значения справа в строках-переходах: что выбрано, не заходя в подэкран.
 *
 * @property languages короткие метки включённых раскладок, например «RU, EN».
 * @property fontsTotal сколько шрифтов есть в каталоге.
 * @property isKeyboardReady клавиатура включена в системе и выбрана текущей.
 */
data class SettingsSummary(
    val languages: String = "",
    val fontsTotal: Int = 0,
    val isKeyboardReady: Boolean = false,
)
