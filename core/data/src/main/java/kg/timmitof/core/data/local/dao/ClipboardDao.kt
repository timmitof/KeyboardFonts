package kg.timmitof.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kg.timmitof.core.data.local.entities.ClipboardEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Запросы по буферу обмена.
 */
@Dao
interface ClipboardDao {

    @Query("SELECT * FROM ${ClipboardEntryEntity.TABLE} ORDER BY isPinned DESC, copiedAt DESC")
    fun observeAll(): Flow<List<ClipboardEntryEntity>>

    @Transaction
    suspend fun capture(text: String, copiedAt: Long, recentLimit: Int) {
        if (touch(text, copiedAt) == 0) {
            insert(ClipboardEntryEntity(text = text, copiedAt = copiedAt))
        }
        trimRecent(recentLimit)
    }

    @Query("UPDATE ${ClipboardEntryEntity.TABLE} SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean)

    @Query("DELETE FROM ${ClipboardEntryEntity.TABLE} WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM ${ClipboardEntryEntity.TABLE} WHERE isPinned = 0")
    suspend fun deleteRecent()

    @Insert
    suspend fun insert(entry: ClipboardEntryEntity)

    @Query("UPDATE ${ClipboardEntryEntity.TABLE} SET copiedAt = :copiedAt WHERE text = :text")
    suspend fun touch(text: String, copiedAt: Long): Int

    @Query(
        """
        DELETE FROM ${ClipboardEntryEntity.TABLE}
        WHERE isPinned = 0 AND id NOT IN (
            SELECT id FROM ${ClipboardEntryEntity.TABLE}
            WHERE isPinned = 0
            ORDER BY copiedAt DESC
            LIMIT :limit
        )
        """
    )
    suspend fun trimRecent(limit: Int)
}
