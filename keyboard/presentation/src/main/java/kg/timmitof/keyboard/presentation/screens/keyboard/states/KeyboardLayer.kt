package kg.timmitof.keyboard.presentation.screens.keyboard.states

internal enum class KeyboardLayer(
    val fixedLayoutName: String? = null,
    val usesLanguageLayout: Boolean = false,
) {
    LETTERS(usesLanguageLayout = true),
    SYMBOLS("symbols"),
    SYMBOLS_ALT("symbols_alt"),
    EMOJI,
    EMOJI_SEARCH(usesLanguageLayout = true);

    val showsSuggestions: Boolean get() = this != EMOJI && this != EMOJI_SEARCH
}