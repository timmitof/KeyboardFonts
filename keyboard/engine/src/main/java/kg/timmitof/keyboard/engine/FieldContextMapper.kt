package kg.timmitof.keyboard.engine

import android.text.InputType
import android.view.inputmethod.EditorInfo
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardFieldContext
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardFieldType

/**
 * Разбирает `EditorInfo` в контекст поля для клавиатуры.
 */
internal fun EditorInfo?.toFieldContext(): KeyboardFieldContext = KeyboardFieldContext(
    type = toFieldType(),
    enterAction = toEnterAction(),
    isMultiLine = isMultiLine(),
)

private fun EditorInfo?.toFieldType(): KeyboardFieldType {
    if (this == null) return KeyboardFieldType.TEXT

    val variation = inputType and InputType.TYPE_MASK_VARIATION

    return when (inputType and InputType.TYPE_MASK_CLASS) {
        InputType.TYPE_CLASS_PHONE -> KeyboardFieldType.PHONE

        InputType.TYPE_CLASS_NUMBER,
        InputType.TYPE_CLASS_DATETIME -> KeyboardFieldType.NUMBER

        InputType.TYPE_CLASS_TEXT -> when (variation) {
            InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD -> KeyboardFieldType.PASSWORD

            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_URI -> KeyboardFieldType.EMAIL

            else -> KeyboardFieldType.TEXT
        }

        else -> KeyboardFieldType.TEXT
    }
}

/** Многострочное поле: Enter переносит строку, а отправка остаётся кнопкой в приложении. */
private fun EditorInfo?.isMultiLine(): Boolean {
    if (this == null) return false
    if (inputType and InputType.TYPE_MASK_CLASS != InputType.TYPE_CLASS_TEXT) return false

    val multiLineFlags = InputType.TYPE_TEXT_FLAG_MULTI_LINE or
        InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE

    return inputType and multiLineFlags != 0
}
