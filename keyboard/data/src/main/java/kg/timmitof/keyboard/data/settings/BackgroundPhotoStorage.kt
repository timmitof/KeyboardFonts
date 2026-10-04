package kg.timmitof.keyboard.data.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Копия уменьшается до размеров клавиатуры: оригинал с камеры весит мегабайты и грузится долго. */
@Singleton
class BackgroundPhotoStorage @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private val directory = File(context.filesDir, DIRECTORY)

    /** Новое имя на каждый импорт, чтобы кэш картинок не отдал старое фото. */
    suspend fun import(uri: String): String = withContext(Dispatchers.IO) {
        val bitmap = decode(Uri.parse(uri))
        directory.mkdirs()

        val file = File(directory, "photo_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
        bitmap.recycle()

        directory.listFiles()?.filter { it != file }?.forEach(File::delete)
        file.absolutePath
    }

    private fun decode(uri: Uri): Bitmap {
        // ImageDecoder сам учитывает поворот снимка из EXIF.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val scale = scaleFor(info.size.width, info.size.height)
                decoder.setTargetSize((info.size.width * scale).toInt(), (info.size.height * scale).toInt())
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: error("Не удалось прочитать картинку $uri")
    }

    private fun scaleFor(width: Int, height: Int): Float =
        (MAX_SIDE.toFloat() / maxOf(width, height)).coerceAtMost(1f)

    private companion object {
        const val DIRECTORY = "keyboard_backgrounds"
        const val MAX_SIDE = 1600
        const val JPEG_QUALITY = 88
    }
}
