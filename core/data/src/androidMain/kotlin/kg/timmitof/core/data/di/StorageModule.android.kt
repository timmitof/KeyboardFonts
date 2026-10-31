package kg.timmitof.core.data.di

import androidx.room.Room
import kg.timmitof.core.data.local.AppDatabase
import kg.timmitof.core.data.preferences.PreferencesDataStoreFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformStorageModule: Module = module {

    single { Room.databaseBuilder(androidContext(), AppDatabase::class.java, AppDatabase.TAG) }

    // Каталог `preferencesDataStore`: настройки, сохранённые до перехода на общий код, остаются на месте.
    single { PreferencesDataStoreFactory(androidContext().filesDir.resolve("datastore").absolutePath) }
}
