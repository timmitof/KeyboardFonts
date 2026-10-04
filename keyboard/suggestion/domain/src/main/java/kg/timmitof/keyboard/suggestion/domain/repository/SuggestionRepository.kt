package kg.timmitof.keyboard.suggestion.domain.repository

import kg.timmitof.keyboard.suggestion.domain.model.SuggestionRequest
import kg.timmitof.keyboard.suggestion.domain.model.WordSuggestion

interface SuggestionRepository {

    suspend fun suggest(request: SuggestionRequest): List<WordSuggestion>

    suspend fun learn(languageCode: String, previousWord: String, word: String)

    suspend fun prefetch(languageCode: String)
}
