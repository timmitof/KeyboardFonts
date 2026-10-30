package kg.timmitof.keyboard.font.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kg.timmitof.keyboard.data.language.keyboardPreferences
import kotlinx.coroutines.flow.first

class SelectedFontDataSource(
    private val context: Context
) {

    suspend fun get(): String? =
        context.keyboardPreferences.data.first()[SELECTED_FONT_KEY]

    suspend fun set(id: String) {
        context.keyboardPreferences.edit { prefs -> prefs[SELECTED_FONT_KEY] = id }
    }

    private companion object {
        val SELECTED_FONT_KEY = stringPreferencesKey("selected_font")
    }
}
