package kg.timmitof.keyboard.presentation.screens.keyboard.states

/**
 * Слои клавиатуры.
 *
 * @property fixedLayoutName фиксированная JSON-раскладка слоя; null — слой раскладку не задаёт.
 * @property usesLanguageLayout слой использует раскладку выбранного языка (см. [KeyboardState.selectedLanguage]).
 */
internal enum class KeyboardLayer(
    val fixedLayoutName: String? = null,
    val usesLanguageLayout: Boolean = false,
) {
    LETTERS(usesLanguageLayout = true),
    SYMBOLS("symbols"),
    SYMBOLS_ALT("symbols_alt"),
    EMOJI,
    EMOJI_SEARCH(usesLanguageLayout = true),
}