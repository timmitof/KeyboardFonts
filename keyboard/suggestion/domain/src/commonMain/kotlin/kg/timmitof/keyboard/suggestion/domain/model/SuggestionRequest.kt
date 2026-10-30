package kg.timmitof.keyboard.suggestion.domain.model

data class SuggestionRequest(
    val languageCode: String,
    val context: TextContext,
    val isShifted: Boolean = false,
    val allowsAutoCorrect: Boolean = true,
)
