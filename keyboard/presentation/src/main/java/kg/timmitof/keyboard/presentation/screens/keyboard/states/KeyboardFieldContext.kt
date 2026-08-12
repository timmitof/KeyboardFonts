package kg.timmitof.keyboard.presentation.screens.keyboard.states

/**
 * IME-сервис отдаёт клавиатуре.
 *
 * Собирается из `EditorInfo` в engine-слое.
 *
 * @param type тип поля — от него зависят раскладка, шрифты и подсказки.
 * @param enterAction что делает Enter; заодно решает, красить ли его акцентом.
 * @param isMultiLine поле многострочное — Enter переносит строку, а не отправляет.
 */
data class KeyboardFieldContext(
    val type: KeyboardFieldType = KeyboardFieldType.TEXT,
    val enterAction: EnterAction = EnterAction.RETURN,
    val isMultiLine: Boolean = false,
)
