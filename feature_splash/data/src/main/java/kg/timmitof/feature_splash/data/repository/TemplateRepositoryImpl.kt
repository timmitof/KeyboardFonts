package kg.timmitof.feature_splash.data.repository

import kg.timmitof.core.data.local.dao.TemplateDao
import kg.timmitof.feature_splash.data.manager.AssetManager
import kg.timmitof.feature_splash.data.mapper.toEntity
import kg.timmitof.feature_splash.domain.repository.TemplateRepository
import javax.inject.Inject

class TemplateRepositoryImpl @Inject constructor(
    private val templateDao: TemplateDao,
    private val assetManager: AssetManager
) : TemplateRepository {

    override suspend fun loadTemplatesFromAsset() {
        val assetTemplates = assetManager.getAllTemplates().map { it.toEntity() }
        templateDao.insertAll(assetTemplates)
    }

    override suspend fun getTemplateCount() = templateDao.getCount()
}