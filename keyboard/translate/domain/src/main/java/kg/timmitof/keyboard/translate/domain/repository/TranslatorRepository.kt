package kg.timmitof.keyboard.translate.domain.repository

import kg.timmitof.keyboard.translate.domain.model.TranslationLanguage
import kg.timmitof.keyboard.translate.domain.model.TranslationResult
import kg.timmitof.keyboard.translate.domain.model.TranslatorPreferences

/** Перевод на телефоне: модели языков скачиваются один раз, текст никуда не отправляется. */
interface TranslatorRepository {

    /** Все языки переводчика, по алфавиту названий. */
    fun languages(): List<TranslationLanguage>

    /** Язык переводчика для кода раскладки (`ru_ru` → `ru`); null — переводчик этот язык не знает. */
    fun languageForLayout(layoutCode: String): TranslationLanguage?

    fun language(code: String): TranslationLanguage?

    /** Коды языков, модели которых уже на телефоне. */
    suspend fun downloadedCodes(): Set<String>

    /** Скачивает модели обоих языков пары. @return удалось ли. */
    suspend fun download(sourceCode: String, targetCode: String): Boolean

    /** Без скачанных моделей не переводит и ничего не качает сам — см. [download]. */
    suspend fun translate(text: String, sourceCode: String, targetCode: String): TranslationResult

    suspend fun getPreferences(): TranslatorPreferences

    suspend fun setTarget(code: String)

    suspend fun setStyled(isStyled: Boolean)
}
