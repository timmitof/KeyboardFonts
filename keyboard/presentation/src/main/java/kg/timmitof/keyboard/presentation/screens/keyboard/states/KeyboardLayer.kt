package kg.timmitof.keyboard.presentation.screens.keyboard.states

/**
 * Слои клавиатуры.
 *
 * @param layoutName имя JSON-раскладки слоя в ассетах;
 * null — слой без раскладки (сохраняет текущую).
 */
internal enum class KeyboardLayer(val layoutName: String?) {
    LETTERS("en_us"),
    SYMBOLS("symbols"),
    SYMBOLS_ALT("symbols_alt"),
    EMOJI(null),
    EMOJI_SEARCH("en_us"),
}