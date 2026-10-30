package kg.timmitof.keyboard.font.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kg.timmitof.keyboard.data.language.keyboardPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** `null` — пользователь панель не настраивал. */
class FontPanelDataSource(
    private val context: Context
) {

    fun observe(): Flow<List<String>?> = context.keyboardPreferences.data.map { prefs ->
        prefs[PANEL_FONTS_KEY]?.split(SEPARATOR)?.filter(String::isNotEmpty)
    }

    suspend fun set(ids: List<String>) {
        context.keyboardPreferences.edit { prefs -> prefs[PANEL_FONTS_KEY] = ids.joinToString(SEPARATOR) }
    }

    suspend fun clear() {
        context.keyboardPreferences.edit { prefs -> prefs.remove(PANEL_FONTS_KEY) }
    }

    private companion object {
        val PANEL_FONTS_KEY = stringPreferencesKey("panel_fonts")
        const val SEPARATOR = ","
    }
}
