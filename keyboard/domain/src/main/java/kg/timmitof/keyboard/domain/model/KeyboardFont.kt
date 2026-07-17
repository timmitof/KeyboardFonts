package kg.timmitof.keyboard.domain.model

/**
 * Шрифт клавиатуры — Unicode-стилизация вводимых букв и цифр.
 */
data class KeyboardFont(
    val id: String,
    val charMap: Map<Char, String>,
) {

    /** Обычный шрифт без стилизации. */
    val isDefault: Boolean get() = charMap.isEmpty()

    fun apply(text: String): String =
        if (charMap.isEmpty()) {
            text
        } else {
            buildString(text.length) {
                text.forEach { char -> append(charMap[char] ?: char.toString()) }
            }
        }

    companion object {
        const val DEFAULT_ID = "default"
        val Default = KeyboardFont(DEFAULT_ID, emptyMap())
    }
}
