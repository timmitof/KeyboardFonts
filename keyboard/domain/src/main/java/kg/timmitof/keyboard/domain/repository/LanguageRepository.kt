package kg.timmitof.keyboard.domain.repository

import kg.timmitof.keyboard.domain.model.KeyboardLanguage

interface LanguageRepository {

    suspend fun getLanguages(): List<KeyboardLanguage>

    suspend fun getSelectedLanguage(): KeyboardLanguage

    suspend fun setSelectedLanguage(code: String)
}
