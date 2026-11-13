package kg.timmitof.keyboard.translate.domain.repository

import kg.timmitof.keyboard.translate.domain.model.TranslationLanguage
import kotlinx.coroutines.flow.Flow

/**
 * Языки переводчика и их модели на телефоне. Модель качается один раз (около 30 МБ),
 * дальше перевод работает без сети; английский встроен и скачан всегда.
 */
interface TranslationModelRepository {

    /** Все языки переводчика, по алфавиту названий. */
    fun languages(): List<TranslationLanguage>

    fun language(code: String): TranslationLanguage?

    /** Язык переводчика для кода раскладки (`ru_ru` → `ru`); null — переводчик этот язык не знает. */
    fun languageForLayout(layoutCode: String): TranslationLanguage?

    /** Коды скачанных языков; новое значение — после каждого скачивания или удаления. */
    fun observeDownloaded(): Flow<Set<String>>

    suspend fun downloadedCodes(): Set<String>

    /** Скачивает недостающие модели. @return все ли теперь на телефоне. */
    suspend fun download(codes: Collection<String>): Boolean

    /** @return удалось ли; встроенный язык не удаляется. */
    suspend fun delete(code: String): Boolean
}
