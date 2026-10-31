package kg.timmitof.keyboard.data.language

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kg.timmitof.core.data.preferences.PreferencesDataStoreFactory

/** Общие настройки клавиатуры: язык, шрифт, внешний вид. */
class KeyboardPreferences(
    factory: PreferencesDataStoreFactory,
) : DataStore<Preferences> by factory.create(NAME) {

    private companion object {
        const val NAME = "keyboard_preferences"
    }
}
