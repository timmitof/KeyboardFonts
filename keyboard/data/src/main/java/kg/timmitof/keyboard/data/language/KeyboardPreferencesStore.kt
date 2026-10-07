package kg.timmitof.keyboard.data.language

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.keyboardPreferences by preferencesDataStore(name = "keyboard_preferences")
