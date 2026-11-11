package kg.timmitof.keyboard.suggestion.domain.repository

import kg.timmitof.keyboard.suggestion.domain.model.KeyGeometry
import kg.timmitof.keyboard.suggestion.domain.model.SuggestionRequest
import kg.timmitof.keyboard.suggestion.domain.model.TypedKey
import kg.timmitof.keyboard.suggestion.domain.model.WordSuggestion

interface SuggestionRepository {

    suspend fun suggest(request: SuggestionRequest): List<WordSuggestion>

    /**
     * Учит слово, завершённое пользователем.
     *
     * @param isDeliberate слово выбрано осознанно (тап по подсказке, заглавная посреди предложения):
     * оно учится сразу, без проверки на опечатку.
     * @param touches касания букв, если слово набрано как есть: по верным словам калибруется,
     * куда пользователь попадает по клавишам.
     */
    suspend fun learn(
        languageCode: String,
        previousWord: String,
        word: String,
        isDeliberate: Boolean = false,
        touches: List<TypedKey> = emptyList(),
    )

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

    /**
     * Умные зоны клавиш: палец лёг у самого края клавиши, а слово с соседней буквой продолжается
     * заметно вероятнее — вернуть соседа. Решает словарь по началу слова [composingWord].
     *
     * @return буква-сосед с тем же касанием относительно её клавиши; `null` — печатать нажатую.
     * Модель языка ещё не загружена — тоже `null`: ввод её не ждёт.
     */
    suspend fun adaptKey(
        languageCode: String,
        geometry: KeyGeometry,
        composingWord: String,
        touch: TypedKey,
    ): TypedKey?

    suspend fun prefetch(languageCode: String)

    /** Немедленно сохраняет всё, что выучено, но ещё не записано на диск. */
    suspend fun flush()
}
