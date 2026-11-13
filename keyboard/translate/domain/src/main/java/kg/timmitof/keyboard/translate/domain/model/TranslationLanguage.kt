package kg.timmitof.keyboard.translate.domain.model

/**
 * Язык переводчика.
 *
 * @param code тег языка переводчика (`en`, `ru`, `zh`), а не код раскладки.
 * @param name название на самом языке: «English», «Deutsch», «中文».
 */
data class TranslationLanguage(
    val code: String,
    val name: String,
) {
    /** Короткая метка для плашки: `EN`, `DE`. */
    val shortName: String get() = code.uppercase()
}
