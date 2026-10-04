package kg.timmitof.feature_splash.data.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kg.timmitof.feature_splash.domain.model.TemplateModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

class AssetManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val assetManager = context.assets

    fun listTemplateFiles(): List<String> {
        return try {
            assetManager.list(TEMPLATE_FOLDER)?.toList().orEmpty()
        } catch (e: IOException) {
            emptyList()
        }
    }

    fun openTemplateStream(fileName: String): InputStream {
        return assetManager.open("$TEMPLATE_FOLDER/$fileName")
    }

    companion object {
        private const val TEMPLATE_FOLDER = "templates"
    }
}