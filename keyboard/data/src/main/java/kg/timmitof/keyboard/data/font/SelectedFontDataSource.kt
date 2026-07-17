package kg.timmitof.keyboard.data.font

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kg.timmitof.keyboard.data.language.keyboardPreferences
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** Хранилище id выбранного шрифта поверх DataStore Preferences. */
@Singleton
class SelectedFontDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    /** Сохранённый id шрифта или null, если пользователь ещё не выбирал. */
    suspend fun get(): String? =
        context.keyboardPreferences.data.first()[SELECTED_FONT_KEY]

    suspend fun set(id: String) {
        context.keyboardPreferences.edit { prefs -> prefs[SELECTED_FONT_KEY] = id }
    }

    private companion object {
        val SELECTED_FONT_KEY = stringPreferencesKey("selected_font")
    }
}
