package kg.timmitof.keyboard.data.language

import com.google.gson.Gson
import kg.timmitof.keyboard.data.AssetTextLoader
import kg.timmitof.keyboard.data.models.LanguageCatalogDto
import kg.timmitof.keyboard.data.models.LanguageDto
import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Каталог языков из `assets/languages.json`. Файл собирает при сборке плагин `keyboardfonts.languageCatalog`
 * из `keyboard/build.gradle.kts`; руками его не правим.
 */
@Singleton
class LanguageCatalogDataSource @Inject constructor(
    private val assetTextLoader: AssetTextLoader,
) {

    private val gson = Gson()

    private val mutex = Mutex()

    /** Каталог неизменен на время жизни процесса — разбираем один раз. */
    @Volatile
    private var catalog: List<KeyboardLanguage>? = null

    suspend fun get(): List<KeyboardLanguage> = catalog ?: mutex.withLock {
        catalog ?: load().also { catalog = it }
    }

    private suspend fun load(): List<KeyboardLanguage> {
        val json = assetTextLoader.loadText(FILE_NAME)
            ?: throw IllegalStateException("Language catalog not found: $FILE_NAME")

        val languages = withContext(Dispatchers.Default) {
            gson.fromJson(json, LanguageCatalogDto::class.java)?.languages.orEmpty().map { it.toDomain() }
        }
        check(languages.isNotEmpty()) { "Language catalog is empty: $FILE_NAME" }
        return languages
    }

    private fun LanguageDto.toDomain() = KeyboardLanguage(
        code = code,
        displayName = name,
        shortName = shortName,
        isLatin = isLatin,
        layout = layout,
        hasDictionary = hasDictionary,
    )

    private companion object {
        const val FILE_NAME = "languages.json"
    }
}
