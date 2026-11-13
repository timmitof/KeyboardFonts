package kg.timmitof.keyboard.translate.domain.repository

import kg.timmitof.keyboard.translate.domain.model.TranslationResult

/** Перевод на телефоне: текст никуда не отправляется. */
interface TranslationRepository {

    /** Без скачанных моделей пары не переводит и ничего не качает сам — см. [TranslationModelRepository.download]. */
    suspend fun translate(text: String, sourceCode: String, targetCode: String): TranslationResult

    /** Отпускает загруженную в память модель: клавиатуре память дорога, а переводчик открыт не всегда. */
    fun release()
}
