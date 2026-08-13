package kg.timmitof.core.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kg.timmitof.core.data.local.entities.ClipboardEntryEntity

object Migrations {
    val all
        get() = arrayOf(
            MIGRATION_1_2
        )

    internal val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `${ClipboardEntryEntity.TABLE}` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`text` TEXT NOT NULL, " +
                        "`isPinned` INTEGER NOT NULL, " +
                        "`copiedAt` INTEGER NOT NULL)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_${ClipboardEntryEntity.TABLE}_text` " +
                        "ON `${ClipboardEntryEntity.TABLE}` (`text`)"
            )
        }
    }
}