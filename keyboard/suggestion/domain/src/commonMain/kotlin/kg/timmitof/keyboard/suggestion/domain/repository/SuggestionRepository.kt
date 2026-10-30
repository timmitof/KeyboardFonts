package kg.timmitof.keyboard.suggestion.domain.repository

import kg.timmitof.keyboard.suggestion.domain.model.SuggestionRequest
import kg.timmitof.keyboard.suggestion.domain.model.WordSuggestion

interface SuggestionRepository {

    suspend fun suggest(request: SuggestionRequest): List<WordSuggestion>

    /**
     * Учит слово, завершённое пользователем.
     *
     * @param isDeliberate слово выбрано осознанно (тап по подсказке, заглавная посреди предложения):
     * оно учится сразу, без проверки на опечатку.
     */
    suspend fun learn(languageCode: String, previousWord: String, word: String, isDeliberate: Boolean = false)

    /**
     * Пользователь отменил автозамену [typed] → [corrected]. Откатывает выученное при замене
     * (если оно было — [wasLearned]), запоминает отказ и учит набранное как обычное слово.
     */
    suspend fun rejectAutoCorrection(
        languageCode: String,
        previousWord: String,
        typed: String,
        corrected: String,
        wasLearned: Boolean,
    )

    suspend fun prefetch(languageCode: String)

    /** Немедленно сохраняет всё, что выучено, но ещё не записано на диск. */
    suspend fun flush()
}
