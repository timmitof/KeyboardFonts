package kg.timmitof.keyboard.domain.repository

import kg.timmitof.keyboard.domain.model.SuggestionRequest
import kg.timmitof.keyboard.domain.model.WordSuggestion

/**
 * Подсказки слов (Т9): словарь языка + то, чему клавиатура научилась у пользователя.
 */
interface SuggestionRepository {

    /** Подсказки для текущей позиции курсора. Пустой список — показывать нечего. */
    suspend fun suggest(request: SuggestionRequest): List<WordSuggestion>

    /**
     * Запоминает законченное слово и его связь с предыдущим.
     *
     * Отсюда берётся адаптация под конкретный диалог: часто набираемые слова
     * и пары слов начинают опережать словарные.
     */
    suspend fun learn(languageCode: String, previousWord: String, word: String)

    /** Готовит словарь заранее, чтобы первая подсказка не ждала разбора ассета. */
    suspend fun prefetch(languageCode: String)
}
