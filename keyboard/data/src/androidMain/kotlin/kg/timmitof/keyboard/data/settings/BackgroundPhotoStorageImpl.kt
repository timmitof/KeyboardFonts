package kg.timmitof.keyboard.data.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.MimeTypeMap
import kg.timmitof.keyboard.data.settings.BackgroundPhotoStorage.StoredPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class BackgroundPhotoStorageImpl(
    private val context: Context,
) : BackgroundPhotoStorage {

    private val directory = File(context.filesDir, DIRECTORY)

    override suspend fun copy(uri: String): StoredPhoto = withContext(Dispatchers.IO) {
        val source = Uri.parse(uri)
        directory.mkdirs()

        val file = File(directory, "photo_${System.currentTimeMillis()}.${extensionOf(source)}")
        try {
            val input = context.contentResolver.openInputStream(source) ?: error("Не удалось открыть $uri")
            input.use { stream -> file.outputStream().use { stream.copyTo(it) } }

            StoredPhoto(path = file.absolutePath, tone = averageColor(file))
        } catch (error: Throwable) {
            // Недокопированный файл никто не удалит — ссылки на него ещё нет.
            file.delete()
            throw error
        }
    }

    override suspend fun delete(path: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(path)
        file.parentFile != directory || !file.exists() || file.delete()
    }

    override fun discard(path: String) {
        File(path).takeIf { it.parentFile == directory }?.delete()
    }

    private fun extensionOf(uri: Uri): String {
        val type = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(type) ?: DEFAULT_EXTENSION
    }

    /** Для оттенка хватает крошечной копии: картинка, ужатая до пикселя, и есть её средний цвет. */
    private fun averageColor(file: File): Long {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= TONE_SIDE) sample *= 2

        val small = BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: return FALLBACK_TONE
        val pixel = Bitmap.createScaledBitmap(small, 1, 1, true)
        val color = pixel.getPixel(0, 0)
        if (pixel !== small) pixel.recycle()
        small.recycle()
        return color.toLong() and 0xFFFFFFFFL
    }

    private companion object {
        const val DIRECTORY = "backgrounds"
        const val DEFAULT_EXTENSION = "jpg"
        const val TONE_SIDE = 64
        const val FALLBACK_TONE = 0xFF808080L
    }
}
