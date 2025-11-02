package kg.timmitof.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kg.timmitof.core.data.local.entities.TemplateEntity

/**
 * Запросы по шаблонам в БД
 */
@Dao
interface TemplateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<TemplateEntity>)

    @Insert
    suspend fun saveTemplate(projectEntity: TemplateEntity): Long

    @Query("DELETE FROM TemplateEntity WHERE id = :id")
    suspend fun deleteTemplate(id: Long)

    @Query("SELECT * FROM TemplateEntity")
    suspend fun getAllTemplates(): List<TemplateEntity>

    @Query("SELECT * FROM TemplateEntity WHERE id = :id")
    suspend fun getTemplateById(id: Long): TemplateEntity?

    @Query("SELECT COUNT(*) FROM TemplateEntity")
    suspend fun getCount(): Int
}