package kg.timmitof.keyboard.data.emoji

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

/** Общий DataStore Preferences для настроек эмодзи (недавние, выбранные тона кожи). */
internal val Context.emojiPreferences by preferencesDataStore(name = "emoji_preferences")
