package kg.timmitof.core.data.di

import androidx.room.RoomDatabase
import kg.timmitof.core.data.local.AppDatabase
import kg.timmitof.core.data.local.Migrations
import org.koin.core.module.Module
import org.koin.dsl.module

val storageModule = module {

    includes(platformStorageModule)

    single {
        get<RoomDatabase.Builder<AppDatabase>>()
            .addMigrations(*Migrations.all)
            .build()
    }

    single { get<AppDatabase>().clipboardDao() }

    single { get<AppDatabase>().backgroundPhotoDao() }
}

/** Платформенная часть: файл базы и каталог настроек. */
internal expect val platformStorageModule: Module
