package kg.timmitof.feature_home.data.source

import kg.timmitof.core.data.local.dao.ProjectDao
import kg.timmitof.core.data.local.entities.UserProjectEntity
import javax.inject.Inject

class ProjectLocalDataSource @Inject constructor(
    private val projectDao: ProjectDao
) {
    suspend fun getAllProjects(): List<UserProjectEntity> = projectDao.getAllUserProjects()
}