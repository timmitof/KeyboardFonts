package kg.timmitof.keyboard.data.repository

import kg.timmitof.keyboard.data.language.SelectedLanguageDataSource
import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import kg.timmitof.keyboard.domain.repository.LanguageRepository
import javax.inject.Inject

class LanguageRepositoryImpl @Inject constructor(
    private val selectedLanguageDataSource: SelectedLanguageDataSource,
    private val keyboardLayoutRepository: KeyboardLayoutRepository,
) : LanguageRepository {

    override suspend fun getLanguages(): List<KeyboardLanguage> =
        LANGUAGE_CODES.map { code ->
            KeyboardLanguage(
                code = code,
                displayName = keyboardLayoutRepository.getLayout(code).name,
                shortName = code.substringBefore('_').uppercase()
            )
        }

    override suspend fun getSelectedLanguage(): KeyboardLanguage {
        val languages = getLanguages()
        val savedCode = selectedLanguageDataSource.get()

        return languages.firstOrNull { it.code == savedCode } ?: languages.first()
    }

    override suspend fun setSelectedLanguage(code: String) = selectedLanguageDataSource.set(code)

    private companion object {
        /** Доступные раскладки; первый код — язык по умолчанию. */
        val LANGUAGE_CODES = listOf("ru_ru", "en_us")
    }
}
