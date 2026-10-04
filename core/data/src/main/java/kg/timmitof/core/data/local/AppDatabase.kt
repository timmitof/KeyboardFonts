package kg.timmitof.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import kg.timmitof.core.data.local.dao.BackgroundPhotoDao
import kg.timmitof.core.data.local.dao.ClipboardDao
import kg.timmitof.core.data.local.entities.BackgroundPhotoEntity
import kg.timmitof.core.data.local.entities.ClipboardEntryEntity

@Database(
    entities = [
        ClipboardEntryEntity::class,
        BackgroundPhotoEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun clipboardDao(): ClipboardDao

    abstract fun backgroundPhotoDao(): BackgroundPhotoDao

    companion object {
        const val TAG = "keyboard-fonts-database"
    }
}
