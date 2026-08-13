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
import kg.timmitof.core.data.local.dao.ClipboardDao
import kg.timmitof.core.data.local.dao.ProjectDao
import kg.timmitof.core.data.local.dao.TemplateDao
import javax.inject.Singleton

/**
 * DI модуль для работы с базой данных
 */
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
    fun provideUserProjectDao(database: AppDatabase): ProjectDao = database.projectDao()

    @Singleton
    @Provides
    fun provideTemplateDao(database: AppDatabase): TemplateDao = database.templateDao()

    @Singleton
    @Provides
    fun provideClipboardDao(database: AppDatabase): ClipboardDao = database.clipboardDao()
}