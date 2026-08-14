package kg.timmitof.keyboard.suggestion.domain.model

/**
 * Запрос подсказок: язык + всё, что клавиатура знает о поле ввода.
 *
 * @param languageCode код раскладки — по нему выбирается словарь.
 * @param context текст вокруг курсора.
 * @param isShifted поднят ли Shift — подсказки придут с заглавной буквы.
 * @param allowsAutoCorrect разрешено ли исправлять слово при вводе пробела.
 */
data class SuggestionRequest(
    val languageCode: String,
    val context: TextContext,
    val isShifted: Boolean = false,
    val allowsAutoCorrect: Boolean = true,
)
