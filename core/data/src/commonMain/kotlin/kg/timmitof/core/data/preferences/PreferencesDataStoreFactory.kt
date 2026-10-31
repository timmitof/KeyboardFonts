package kg.timmitof.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kg.timmitof.core.data.AppDirectory
import okio.Path.Companion.toPath

/**
 * Файлы настроек `datastore/<name>.preferences_pb`. На Android это каталог `preferencesDataStore`,
 * поэтому сохранённое раньше читается без миграции. Хранилище на файл создаётся один раз за процесс.
 */
class PreferencesDataStoreFactory(private val appDirectory: AppDirectory) {

    fun create(name: String): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
        produceFile = { "${appDirectory.path}/$DIRECTORY/$name.preferences_pb".toPath() },
    )

    private companion object {
        const val DIRECTORY = "datastore"
    }
}
