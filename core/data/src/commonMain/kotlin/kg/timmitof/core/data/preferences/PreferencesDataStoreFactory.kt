package kg.timmitof.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

/**
 * Файлы настроек `<directory>/<name>.preferences_pb`. На Android каталог тот же, что у `preferencesDataStore`,
 * поэтому сохранённое раньше читается без миграции. Хранилище на файл создаётся один раз за процесс.
 */
class PreferencesDataStoreFactory(private val directory: String) {

    fun create(name: String): DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(produceFile = { "$directory/$name.preferences_pb".toPath() })
}
