package kg.timmitof.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kg.timmitof.core.data.local.entities.BackgroundPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BackgroundPhotoDao {

    @Query("SELECT * FROM ${BackgroundPhotoEntity.TABLE} ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<BackgroundPhotoEntity>>

    @Query("SELECT * FROM ${BackgroundPhotoEntity.TABLE} WHERE id = :id")
    suspend fun getById(id: Long): BackgroundPhotoEntity?

    @Insert
    suspend fun insert(photo: BackgroundPhotoEntity): Long

    @Query(
        """
        UPDATE ${BackgroundPhotoEntity.TABLE}
        SET cropLeft = :left, cropTop = :top, cropRight = :right, cropBottom = :bottom
        WHERE id = :id
        """
    )
    suspend fun updateCrop(id: Long, left: Float, top: Float, right: Float, bottom: Float)

    @Query("DELETE FROM ${BackgroundPhotoEntity.TABLE} WHERE id = :id")
    suspend fun delete(id: Long)
}
