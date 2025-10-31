package kg.timmitof.core.data.repository

import kg.timmitof.core.data.manager.AssetManager
import kg.timmitof.core.domain.repository.BackgroundRepository
import javax.inject.Inject

class BackgroundRepositoryImpl @Inject constructor(
    val assetManager: AssetManager
) : BackgroundRepository {

    override suspend fun getBackgrounds() = assetManager.getBackgrounds()
}