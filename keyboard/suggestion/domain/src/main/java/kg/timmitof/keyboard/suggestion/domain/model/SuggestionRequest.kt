package kg.timmitof.keyboard.suggestion.domain.model

/**
 * @param geometry раскладка, на которой набрано слово: по ней считается, какие буквы рядом.
 * @param touches касания букв набираемого слова по порядку; не совпали со словом — не учитываются.
 */
data class SuggestionRequest(
    val languageCode: String,
    val context: TextContext,
    val isShifted: Boolean = false,
    val allowsAutoCorrect: Boolean = true,
    val geometry: KeyGeometry? = null,
    val touches: List<TypedKey> = emptyList(),
)
