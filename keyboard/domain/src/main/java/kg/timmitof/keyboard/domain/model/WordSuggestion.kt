package kg.timmitof.keyboard.domain.model

/**
 * Подсказка слова для строки над клавишами.
 *
 * @param text слово, которое подставится в поле.
 * @param isLiteral левый слот — ровно то, что набрано (показывается в кавычках и не заменяется).
 * @param isAutoCorrect центральный слот исправляет опечатку: подсвечивается акцентом,
 * потому что текст изменится сам, без участия пользователя.
 */
data class WordSuggestion(
    val text: String,
    val isLiteral: Boolean = false,
    val isAutoCorrect: Boolean = false,
)
