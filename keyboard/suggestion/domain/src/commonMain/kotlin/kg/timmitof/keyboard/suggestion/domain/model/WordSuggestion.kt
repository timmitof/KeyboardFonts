package kg.timmitof.keyboard.suggestion.domain.model

data class WordSuggestion(
    val text: String,
    val isLiteral: Boolean = false,
    val isAutoCorrect: Boolean = false,
)
