package kg.timmitof.feature_splash.data.manager

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kg.timmitof.feature_splash.domain.model.TemplateModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

/**
 * Класс помощник для работы с ассетами
 */
class AssetManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val assetManager = context.assets

    suspend fun getAllTemplates(): List<TemplateModel> = withContext(Dispatchers.IO) {
        try {
            val imageNames = assetManager.list(TEMPLATE_FOLDER).orEmpty()
            imageNames.mapNotNull { imageName ->
                val filePath = "file:///android_asset/$TEMPLATE_FOLDER/$imageName"
                TemplateModel(bitmapFilePath = filePath, name = imageName)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }

    companion object {
        private const val TEMPLATE_FOLDER = "templates"
    }
}