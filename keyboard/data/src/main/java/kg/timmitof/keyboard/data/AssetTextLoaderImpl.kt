package kg.timmitof.keyboard.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
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

    /**
     * `openFd` работает только для несжатого ассета; сжатый (или отсутствующий) бросает исключение —
     * тогда читаем как обычно. Отображение живёт и после закрытия дескриптора.
     */
    override suspend fun loadBuffer(path: String): ByteBuffer? = withContext(Dispatchers.IO) {
        try {
            assetManager.openFd(path).use { descriptor ->
                FileInputStream(descriptor.fileDescriptor).use { stream ->
                    stream.channel.map(FileChannel.MapMode.READ_ONLY, descriptor.startOffset, descriptor.declaredLength)
                }
            }
        } catch (e: IOException) {
            loadBytes(path)?.let(ByteBuffer::wrap)
        }
    }
}
