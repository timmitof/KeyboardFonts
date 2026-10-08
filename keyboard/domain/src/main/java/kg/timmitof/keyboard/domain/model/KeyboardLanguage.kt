package kg.timmitof.keyboard.domain.model

/**
 * Язык из каталога (`assets/languages.json`).
 *
 * @param isLatin поля вроде адреса почты и пароля обычно не принимают другие алфавиты.
 * @param layout имя раскладки в `assets/layouts/`.
 * @param hasDictionary есть ли словарь Т9; без него язык работает без подсказок и автозамены.
 */
data class KeyboardLanguage(
    val code: String,
    val displayName: String,
    val shortName: String,
    val isLatin: Boolean = false,
    val layout: String = code,
    val hasDictionary: Boolean = false,
)

/**
 * Снимок языков: весь каталог, включённые пользователем в порядке переключения и выбранный.
 * [enabled] никогда не пуст, [selected] всегда из [enabled].
 */
data class KeyboardLanguages(
    val catalog: List<KeyboardLanguage>,
    val enabled: List<KeyboardLanguage>,
    val selected: KeyboardLanguage,
) {

    /** Латиница для почты и пароля: сначала из включённых, иначе первая латинская из каталога. */
    val latin: KeyboardLanguage?
        get() = enabled.firstOrNull(KeyboardLanguage::isLatin) ?: catalog.firstOrNull(KeyboardLanguage::isLatin)
}
