package kg.timmitof.keyboard.data.language

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Выбор пользователя как он записан: коды без проверки по каталогу — правила сводит `LanguageRepositoryImpl`.
 *
 * @param enabled включённые языки в порядке переключения; `null` — пользователь список ещё не трогал.
 */
data class LanguagePreferences(
    val selected: String? = null,
    val enabled: List<String>? = null,
)

/** Лежит в общем `keyboardPreferences` рядом с настройками клавиатуры. */
@Singleton
class LanguagePreferencesDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    /** Сбойное чтение не должно валить клавиатуру — откатываемся к языкам по умолчанию. */
    private val preferences: Flow<Preferences> = context.keyboardPreferences.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }

    fun observe(): Flow<LanguagePreferences> = preferences
        .map(::toLanguagePreferences)
        .distinctUntilChanged()

    suspend fun get(): LanguagePreferences = toLanguagePreferences(preferences.first())

    suspend fun setSelected(code: String) {
        context.keyboardPreferences.edit { prefs -> prefs[SELECTED_LANGUAGE_KEY] = code }
    }

    /** Список и выбранный пишутся одной транзакцией: клавиатура не увидит выбранный язык вне списка. */
    suspend fun setEnabled(codes: List<String>, selected: String) {
        context.keyboardPreferences.edit { prefs ->
            prefs[ENABLED_LANGUAGES_KEY] = codes.joinToString(SEPARATOR)
            prefs[SELECTED_LANGUAGE_KEY] = selected
        }
    }

    private fun toLanguagePreferences(prefs: Preferences) = LanguagePreferences(
        selected = prefs[SELECTED_LANGUAGE_KEY],
        enabled = prefs[ENABLED_LANGUAGES_KEY]?.split(SEPARATOR)?.filter(String::isNotEmpty),
    )

    private companion object {
        val SELECTED_LANGUAGE_KEY = stringPreferencesKey("selected_language")

        /** Порядок важен, а у string set его нет — храним строкой через запятую. */
        val ENABLED_LANGUAGES_KEY = stringPreferencesKey("enabled_languages")

        const val SEPARATOR = ","
    }
}
