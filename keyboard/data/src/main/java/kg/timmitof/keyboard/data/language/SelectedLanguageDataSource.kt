package kg.timmitof.keyboard.data.language

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelectedLanguageDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    suspend fun get(): String? =
        context.keyboardPreferences.data.first()[SELECTED_LANGUAGE_KEY]

    suspend fun set(code: String) {
        context.keyboardPreferences.edit { prefs -> prefs[SELECTED_LANGUAGE_KEY] = code }
    }

    private companion object {
        val SELECTED_LANGUAGE_KEY = stringPreferencesKey("selected_language")
    }
}
