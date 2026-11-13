package kg.timmitof.keyboard.presentation.screens.keyboard.states

/**
 * @param isAnchor открывается кнопкой справа в верхней строке.
 * @param isTranslator лист панели перевода: закрывается вместе с ней.
 */
internal enum class KeyboardOverlay(
    val isAnchor: Boolean = true,
    val isTranslator: Boolean = false,
) {
    CLIPBOARD,
    QUICK_SETTINGS,
    /** «Перевести на»: все языки переводчика. */
    TRANSLATION_TARGET(isAnchor = false, isTranslator = true),
    /** «Перевести с»: включённые раскладки — выбор переключает и раскладку. */
    TRANSLATION_SOURCE(isAnchor = false, isTranslator = true),
}
