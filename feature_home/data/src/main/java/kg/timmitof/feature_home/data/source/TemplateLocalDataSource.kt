package kg.timmitof.feature_home.data.source

import kg.timmitof.core.data.local.dao.TemplateDao
import kg.timmitof.core.data.local.entities.TemplateEntity
import javax.inject.Inject

class TemplateLocalDataSource @Inject constructor(
    private val templateDao: TemplateDao
) {
    suspend fun getAllTemplates(): List<TemplateEntity> = templateDao.getAllTemplates()
}