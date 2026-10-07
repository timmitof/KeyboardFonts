package kg.timmitof.keyboard.data.emoji

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

internal val Context.emojiPreferences by preferencesDataStore(name = "emoji_preferences")
