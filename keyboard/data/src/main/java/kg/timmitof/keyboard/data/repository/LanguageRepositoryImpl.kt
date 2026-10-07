package kg.timmitof.keyboard.data.repository

import kg.timmitof.keyboard.data.language.SelectedLanguageDataSource
import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import kg.timmitof.keyboard.domain.repository.LanguageRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class LanguageRepositoryImpl @Inject constructor(
    private val selectedLanguageDataSource: SelectedLanguageDataSource,
    private val keyboardLayoutRepository: KeyboardLayoutRepository,
) : LanguageRepository {

    @Volatile
    private var languages: List<KeyboardLanguage>? = null

    /** Список языков неизменен на время жизни процесса — считаем один раз, раскладки грузим параллельно. */
    override suspend fun getLanguages(): List<KeyboardLanguage> = languages ?: coroutineScope {
        LANGUAGE_CODES.map { code ->
            async {
                val layout = keyboardLayoutRepository.getLayout(code)
                KeyboardLanguage(
                    code = code,
                    displayName = layout.name,
                    shortName = code.substringBefore('_').uppercase(),
                    isLatin = layout.isLatin
                )
            }
        }.awaitAll()
    }.also { languages = it }

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
