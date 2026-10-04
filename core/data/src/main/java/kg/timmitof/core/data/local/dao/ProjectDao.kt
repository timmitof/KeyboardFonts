package kg.timmitof.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kg.timmitof.core.data.local.entities.UserProjectEntity

@Dao
interface ProjectDao {

    @Insert
    suspend fun saveUserProject(projectEntity: UserProjectEntity): Long

    @Query("DELETE FROM UserProjectEntity WHERE id = :id")
    suspend fun deleteUserProject(id: Long)

    @Query("SELECT * FROM UserProjectEntity")
    suspend fun getAllUserProjects(): List<UserProjectEntity>

    @Query("SELECT * FROM UserProjectEntity WHERE id = :id")
    suspend fun getUserProjectById(id: Long): UserProjectEntity?
}