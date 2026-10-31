package kg.timmitof.core.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import kg.timmitof.core.data.local.dao.BackgroundPhotoDao
import kg.timmitof.core.data.local.dao.ClipboardDao
import kg.timmitof.core.data.local.entities.BackgroundPhotoEntity
import kg.timmitof.core.data.local.entities.ClipboardEntryEntity

@Database(
    entities = [
        ClipboardEntryEntity::class,
        BackgroundPhotoEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun clipboardDao(): ClipboardDao

    abstract fun backgroundPhotoDao(): BackgroundPhotoDao

    companion object {
        const val TAG = "keyboard-fonts-database"
    }
}

/** Реализацию для каждой платформы генерирует Room. */
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
