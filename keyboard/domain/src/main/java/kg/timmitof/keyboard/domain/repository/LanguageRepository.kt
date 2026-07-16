package kg.timmitof.keyboard.domain.repository

import kg.timmitof.keyboard.domain.model.KeyboardLanguage

interface LanguageRepository {

    /** Все доступные языковые раскладки. */
    suspend fun getLanguages(): List<KeyboardLanguage>

    /** Текущая выбранная раскладка (сохранённая или язык по умолчанию). */
    suspend fun getSelectedLanguage(): KeyboardLanguage

    /** Сохранить выбор языка по коду раскладки. */
    suspend fun setSelectedLanguage(code: String)
}
