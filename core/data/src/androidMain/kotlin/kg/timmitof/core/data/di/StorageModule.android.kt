package kg.timmitof.core.data.di

import androidx.room.Room
import kg.timmitof.core.data.AppDirectory
import kg.timmitof.core.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformStorageModule: Module = module {

    single { Room.databaseBuilder(androidContext(), AppDatabase::class.java, AppDatabase.TAG) }

    // Тот же `filesDir`, что и до перехода на общий код: настройки и выученные слова остаются на месте.
    single { AppDirectory(androidContext().filesDir.absolutePath) }
}
