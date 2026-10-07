package kg.timmitof.keyboard.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AssetTextLoaderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AssetTextLoader {

    private val assetManager = context.assets

    override suspend fun loadText(path: String): String? = withContext(Dispatchers.IO) {
        try {
            assetManager.open(path).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun loadBytes(path: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            assetManager.open(path).use { it.readBytes() }
        } catch (e: Exception) {
            null
        }
    }
}
