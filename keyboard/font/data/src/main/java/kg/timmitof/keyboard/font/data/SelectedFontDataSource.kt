package kg.timmitof.keyboard.font.data

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kg.timmitof.keyboard.data.language.KeyboardPreferences
import kotlinx.coroutines.flow.first

class SelectedFontDataSource(
    private val keyboardPreferences: KeyboardPreferences
) {

    suspend fun get(): String? =
        keyboardPreferences.data.first()[SELECTED_FONT_KEY]

    suspend fun set(id: String) {
        keyboardPreferences.edit { prefs -> prefs[SELECTED_FONT_KEY] = id }
    }

    private companion object {
        val SELECTED_FONT_KEY = stringPreferencesKey("selected_font")
    }
}
