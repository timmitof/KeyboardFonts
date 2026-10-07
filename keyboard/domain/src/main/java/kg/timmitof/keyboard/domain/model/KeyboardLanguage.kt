package kg.timmitof.keyboard.domain.model

/** [isLatin] — поля вроде адреса почты и пароля обычно не принимают другие алфавиты. */
data class KeyboardLanguage(
    val code: String,
    val displayName: String,
    val shortName: String,
    val isLatin: Boolean = false
)
