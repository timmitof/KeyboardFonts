package kg.timmitof.keyboard.font.domain.model

/**
 * @param charMap подстановка символов: буква → стилизованный глиф.
 * @param marks комбинируемые знаки после каждой буквы и цифры без подстановки — работают с любым алфавитом.
 * @param scripts алфавиты, которые стиль реально поддерживает; на остальных панель его не показывает.
 */
data class KeyboardFont(
    val id: String,
    val charMap: Map<Char, String>,
    val marks: String = "",
    val scripts: Set<FontScript> = FontScript.ALL,
) {

    val isDefault: Boolean get() = charMap.isEmpty() && marks.isEmpty()

    fun supports(script: FontScript): Boolean = script in scripts

    fun apply(text: String): String {
        if (isDefault) return text

        return buildString(text.length * (1 + marks.length)) {
            text.forEach { char ->
                val glyph = charMap[char]
                when {
                    glyph != null -> append(glyph)
                    marks.isNotEmpty() && char.isLetterOrDigit() -> append(char).append(marks)
                    else -> append(char)
                }
            }
        }
    }

    companion object {
        const val DEFAULT_ID = "default"
        val Default = KeyboardFont(DEFAULT_ID, emptyMap())
    }
}
