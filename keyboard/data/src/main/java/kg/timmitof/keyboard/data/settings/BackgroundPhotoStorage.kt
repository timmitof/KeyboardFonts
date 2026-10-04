package kg.timmitof.keyboard.data.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Копия байт в байт: фото остаётся в полном размере и не зависит от галереи. */
@Singleton
class BackgroundPhotoStorage @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private val directory = File(context.filesDir, DIRECTORY)

    suspend fun copy(uri: String): StoredPhoto = withContext(Dispatchers.IO) {
        val source = Uri.parse(uri)
        directory.mkdirs()

        val file = File(directory, "photo_${System.currentTimeMillis()}.${extensionOf(source)}")
        val input = context.contentResolver.openInputStream(source) ?: error("Не удалось открыть $uri")
        input.use { stream -> file.outputStream().use { stream.copyTo(it) } }

        StoredPhoto(path = file.absolutePath, tone = averageColor(file))
    }

    suspend fun delete(path: String) = withContext(Dispatchers.IO) {
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

    data class StoredPhoto(val path: String, val tone: Long)

    private companion object {
        const val DIRECTORY = "backgrounds"
        const val DEFAULT_EXTENSION = "jpg"
        const val TONE_SIDE = 64
        const val FALLBACK_TONE = 0xFF808080L
    }
}
