package kg.timmitof.core.data.manager

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kg.timmitof.core.domain.model.BackgroundModel
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

    suspend fun getBackgrounds(): List<BackgroundModel> = withContext(Dispatchers.IO) {
        try {
            val imageNames = assetManager.list(BACKGROUNDS_FOLDER).orEmpty()
            imageNames.mapNotNull { imageName ->
                val filePath = "file:///android_asset/$BACKGROUNDS_FOLDER/$imageName"
                BackgroundModel(filePath = filePath)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }

    companion object {
        private const val BACKGROUNDS_FOLDER = "backgrounds"
    }
}