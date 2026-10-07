package kg.timmitof.core.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kg.timmitof.core.data.local.AppDatabase
import kg.timmitof.core.data.local.Migrations
import kg.timmitof.core.data.local.dao.BackgroundPhotoDao
import kg.timmitof.core.data.local.dao.ClipboardDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.TAG)
            .addMigrations(*Migrations.all)
            .build()

    @Singleton
    @Provides
    fun provideClipboardDao(database: AppDatabase): ClipboardDao = database.clipboardDao()

    @Singleton
    @Provides
    fun provideBackgroundPhotoDao(database: AppDatabase): BackgroundPhotoDao = database.backgroundPhotoDao()
}