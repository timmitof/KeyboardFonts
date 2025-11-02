package kg.timmitof.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import kg.timmitof.core.data.local.dao.ProjectDao
import kg.timmitof.core.data.local.dao.TemplateDao
import kg.timmitof.core.data.local.entities.TemplateEntity
import kg.timmitof.core.data.local.entities.UserProjectEntity

/**
 * Класс с Room для работы с базой данных, включая сущность, версию, конвертеры и DAO.
 * Константа [TAG] для идентификации базы.
 */
@Database(entities = [TemplateEntity::class, UserProjectEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun projectDao(): ProjectDao

    abstract fun templateDao(): TemplateDao

    companion object {
        const val TAG = "keyboard-fonts-database"
    }
}