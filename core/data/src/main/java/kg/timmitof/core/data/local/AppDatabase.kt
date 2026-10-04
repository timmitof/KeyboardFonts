package kg.timmitof.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import kg.timmitof.core.data.local.dao.ClipboardDao
import kg.timmitof.core.data.local.dao.ProjectDao
import kg.timmitof.core.data.local.dao.TemplateDao
import kg.timmitof.core.data.local.entities.ClipboardEntryEntity
import kg.timmitof.core.data.local.entities.TemplateEntity
import kg.timmitof.core.data.local.entities.UserProjectEntity

@Database(
    entities = [TemplateEntity::class, UserProjectEntity::class, ClipboardEntryEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun projectDao(): ProjectDao

    abstract fun templateDao(): TemplateDao

    abstract fun clipboardDao(): ClipboardDao

    companion object {
        const val TAG = "keyboard-fonts-database"
    }
}
