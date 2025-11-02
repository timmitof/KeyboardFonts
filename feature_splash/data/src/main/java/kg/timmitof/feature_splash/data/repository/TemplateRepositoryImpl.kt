package kg.timmitof.feature_splash.data.repository

import kg.timmitof.core.data.local.StorageManager
import kg.timmitof.core.data.local.dao.TemplateDao
import kg.timmitof.core.data.local.entities.TemplateEntity
import kg.timmitof.feature_splash.data.manager.AssetManager
import kg.timmitof.feature_splash.data.mapper.toDomain
import kg.timmitof.feature_splash.data.mapper.toEntity
import kg.timmitof.feature_splash.domain.model.TemplateModel
import kg.timmitof.feature_splash.domain.repository.TemplateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class TemplateRepositoryImpl @Inject constructor(
    private val templateDao: TemplateDao,
    private val assetManager: AssetManager,
    private val storageManager: StorageManager
) : TemplateRepository {

    override suspend fun loadTemplatesFromAsset() {
        val initializeTemplates = initializeTemplates()
        templateDao.insertAll(initializeTemplates)
    }

    private suspend fun initializeTemplates(): List<TemplateEntity> = withContext(Dispatchers.IO) {
        val imageNames = assetManager.listTemplateFiles()
        imageNames.mapNotNull { fileName ->
            try {
                val inputStream = assetManager.openTemplateStream(fileName)
                val savedPath = storageManager.saveToLocalFiles(fileName, inputStream)

                TemplateEntity(
                    name = fileName,
                    bitmapFilePath = savedPath
                )
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun getTemplateCount() = templateDao.getCount()
}