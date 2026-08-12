package kg.timmitof.keyboard.domain.model

/**
 * Языковая раскладка клавиатуры.
 *
 * @property code код раскладки — имя JSON-файла в assets/layouts (например, "ru_ru").
 * @property displayName полное название языка для пробела и попапа (например, "Русский").
 * @property shortName короткая метка для боковых подсказок на пробеле (например, "RU").
 * @property isLatin раскладка набирает латиницей — такие поля, как адрес почты
 * и пароль, других алфавитов обычно не принимают.
 */
data class KeyboardLanguage(
    val code: String,
    val displayName: String,
    val shortName: String,
    val isLatin: Boolean = false
)
