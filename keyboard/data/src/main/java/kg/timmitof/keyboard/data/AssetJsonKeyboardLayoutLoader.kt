package kg.timmitof.keyboard.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AssetJsonKeyboardLayoutLoader @Inject constructor(
    @param:ApplicationContext private val context: Context
) : JsonKeyboardLayoutLoader {

    private val assetManager = context.assets

    override suspend fun loadKeyboardLayout(filename: String): String? =
        withContext(Dispatchers.IO) {
            return@withContext try {
                assetManager.open("$KEYBOARD_LAYOUT_FOLDER/$filename")
                    .bufferedReader()
                    .use { it.readText() }
            } catch (e: Exception) {
                null
            }
        }

    companion object {
        private const val KEYBOARD_LAYOUT_FOLDER = "layouts"
    }
}