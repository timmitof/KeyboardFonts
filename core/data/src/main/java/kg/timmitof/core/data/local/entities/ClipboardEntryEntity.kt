package kg.timmitof.core.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Запись буфера обмена клавиатуры.
 */
@Entity(
    tableName = ClipboardEntryEntity.TABLE,
    indices = [Index(value = ["text"], unique = true)],
)
data class ClipboardEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val isPinned: Boolean = false,
    val copiedAt: Long = 0L,
) {
    companion object {
        const val TABLE = "clipboard_entries"
    }
}
