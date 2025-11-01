package kg.timmitof.core.data.repository

import kg.timmitof.core.data.manager.AssetManager
import kg.timmitof.core.domain.repository.ProjectRepository
import javax.inject.Inject

class ProjectRepositoryImpl @Inject constructor(
    val assetManager: AssetManager
) : ProjectRepository {

    override suspend fun getAllTemplates() = assetManager.getAllTemplates()
}