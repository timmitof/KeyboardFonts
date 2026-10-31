package kg.timmitof.keyboard.data.emoji

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kg.timmitof.core.data.preferences.PreferencesDataStoreFactory

/** Недавние эмодзи и выбранные варианты тона кожи. */
class EmojiPreferences(
    factory: PreferencesDataStoreFactory,
) : DataStore<Preferences> by factory.create(NAME) {

    private companion object {
        const val NAME = "emoji_preferences"
    }
}
