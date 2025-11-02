package kg.timmitof.feature_home.domain.repository

import kg.timmitof.feature_home.domain.model.UserProjectModel

interface ProjectRepository {

    suspend fun getAllUserProjects(): List<UserProjectModel>
}