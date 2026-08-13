package kg.timmitof.keyboard.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kg.timmitof.keyboard.data.language.keyboardPreferences
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Настройки клавиатуры поверх того же DataStore, где лежат выбранный шрифт и язык.
 *
 * Ключи не перечисляются руками: каждый переключатель знает своё имя, поэтому
 * новая настройка появляется в хранилище вместе с записью в [KeyboardToggle].
 */
@Singleton
class KeyboardSettingsDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    /** Ключи считаются один раз: `booleanPreferencesKey` на каждое чтение — лишняя работа. */
    private val toggleKeys: Map<KeyboardToggle, Preferences.Key<Boolean>> =
        KeyboardToggle.entries.associateWith { booleanPreferencesKey(it.key) }

    /**
     * Сбойное чтение хранилища не должно валить клавиатуру: она откатывается
     * к значениям по умолчанию и продолжает работать.
     */
    private val preferences: Flow<Preferences> = context.keyboardPreferences.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }

    fun observe(): Flow<KeyboardSettings> = preferences.map(::toSettings)

    suspend fun get(): KeyboardSettings = toSettings(preferences.first())

    suspend fun setToggle(toggle: KeyboardToggle, enabled: Boolean) {
        context.keyboardPreferences.edit { prefs ->
            prefs[toggleKeys.getValue(toggle)] = enabled
        }
    }

    suspend fun setTheme(mode: KeyboardThemeMode) {
        context.keyboardPreferences.edit { prefs -> prefs[THEME_KEY] = mode.key }
    }

    private fun toSettings(prefs: Preferences) = KeyboardSettings(
        flags = KeyboardToggle.entries.associateWith { toggle ->
            prefs[toggleKeys.getValue(toggle)] ?: toggle.default
        },
        theme = KeyboardThemeMode.of(prefs[THEME_KEY]),
    )

    private companion object {
        val THEME_KEY = stringPreferencesKey("keyboard_theme")
    }
}
