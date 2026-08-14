package kg.timmitof.keyboard.data.language

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

/** DataStore Preferences для общих настроек клавиатуры (выбранный язык и т. п.). */
val Context.keyboardPreferences by preferencesDataStore(name = "keyboard_preferences")
