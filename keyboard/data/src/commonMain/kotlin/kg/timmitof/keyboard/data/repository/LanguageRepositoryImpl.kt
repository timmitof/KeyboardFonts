package kg.timmitof.keyboard.data.repository

import kg.timmitof.keyboard.data.language.DeviceLocale
import kg.timmitof.keyboard.data.language.LanguageCatalogDataSource
import kg.timmitof.keyboard.data.language.LanguagePreferences
import kg.timmitof.keyboard.data.language.LanguagePreferencesDataSource
import kg.timmitof.keyboard.data.language.deviceLocale
import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.domain.model.KeyboardLanguages
import kg.timmitof.keyboard.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Сводит каталог и записанный выбор пользователя в [KeyboardLanguages]:
 * коды вне каталога отбрасываются, пустой список заменяется языками по умолчанию,
 * выбранный вне списка — первым включённым.
 */
class LanguageRepositoryImpl(
    private val catalogDataSource: LanguageCatalogDataSource,
    private val preferencesDataSource: LanguagePreferencesDataSource,
) : LanguageRepository {

    override suspend fun getLanguages(): KeyboardLanguages =
        resolve(catalogDataSource.get(), preferencesDataSource.get())

    override fun observeLanguages(): Flow<KeyboardLanguages> = preferencesDataSource.observe()
        .map { preferences -> resolve(catalogDataSource.get(), preferences) }
        .distinctUntilChanged()

    override suspend fun setSelectedLanguage(code: String) = preferencesDataSource.setSelected(code)

    override suspend fun setEnabledLanguages(codes: List<String>) {
        val catalog = catalogDataSource.get()
        val enabled = codes.distinct().filter { code -> catalog.any { it.code == code } }
        if (enabled.isEmpty()) return

        val selected = preferencesDataSource.get().selected?.takeIf { it in enabled } ?: enabled.first()
        preferencesDataSource.setEnabled(enabled, selected)
    }

    private fun resolve(catalog: List<KeyboardLanguage>, preferences: LanguagePreferences): KeyboardLanguages {
        val byCode = catalog.associateBy(KeyboardLanguage::code)
        val enabled = preferences.enabled.orEmpty().distinct().mapNotNull(byCode::get)
            .ifEmpty { defaultLanguages(catalog) }
        val selected = enabled.firstOrNull { it.code == preferences.selected } ?: enabled.first()

        return KeyboardLanguages(catalog = catalog, enabled = enabled, selected = selected)
    }

    /** Язык системы из каталога плюс английский; если языка системы в каталоге нет — первый язык каталога. */
    private fun defaultLanguages(catalog: List<KeyboardLanguage>): List<KeyboardLanguage> {
        val system = deviceLocale()
        val systemLanguage = catalog.firstOrNull { it.code.equals(system.toCode(), ignoreCase = true) }
            ?: catalog.firstOrNull { it.language == system.language }
            ?: return listOf(catalog.first())

        val english = catalog.firstOrNull { it.language == ENGLISH }
        return listOfNotNull(systemLanguage, english).distinct()
    }

    private fun DeviceLocale.toCode(): String = "${language}_$country"

    private val KeyboardLanguage.language: String
        get() = code.substringBefore('_').lowercase()

    private companion object {
        const val ENGLISH = "en"
    }
}
