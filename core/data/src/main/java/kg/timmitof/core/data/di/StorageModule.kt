package kg.timmitof.core.data.di

import androidx.room.Room
import kg.timmitof.core.data.local.AppDatabase
import kg.timmitof.core.data.local.Migrations
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val storageModule = module {

    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, AppDatabase.TAG)
            .addMigrations(*Migrations.all)
            .build()
    }

    single { get<AppDatabase>().clipboardDao() }

    single { get<AppDatabase>().backgroundPhotoDao() }
}
