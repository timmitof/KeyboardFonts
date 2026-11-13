package kg.timmitof.keyboard.translate.domain.repository

import kg.timmitof.keyboard.translate.domain.model.DetectedLanguage

/**
 * Определение языка текста на телефоне. Знает больше языков, чем переводчик (в том числе кыргызский,
 * казахский, узбекский, таджикский), поэтому подсказывает и там, где перевести нельзя.
 */
interface LanguageDetectionRepository {

    /** Возможные языки, самые вероятные первыми; пусто — язык не понять (короткий текст, цифры). */
    suspend fun detect(text: String): List<DetectedLanguage>

    /** Отпускает модель из памяти, пока панель перевода закрыта. */
    fun release()
}
