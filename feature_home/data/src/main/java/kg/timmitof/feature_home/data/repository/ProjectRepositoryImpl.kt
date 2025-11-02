package kg.timmitof.feature_home.data.repository

import kg.timmitof.feature_home.data.mapper.toDomain
import kg.timmitof.feature_home.data.source.ProjectLocalDataSource
import kg.timmitof.feature_home.domain.model.UserProjectModel
import kg.timmitof.feature_home.domain.repository.ProjectRepository
import javax.inject.Inject

class ProjectRepositoryImpl @Inject constructor(
    private val projectLocalDataSource: ProjectLocalDataSource
) : ProjectRepository {

    override suspend fun getAllUserProjects(): List<UserProjectModel> =
        projectLocalDataSource.getAllProjects().map { it.toDomain() }
}