package kg.timmitof.keyboard.presentation.screens.keyboard.states

/** @param isAnchor открывается кнопкой справа в верхней строке; выбор языка перевода открывает панель перевода. */
internal enum class KeyboardOverlay(val isAnchor: Boolean = true) {
    CLIPBOARD,
    QUICK_SETTINGS,
    TRANSLATION_LANGUAGES(isAnchor = false),
}
