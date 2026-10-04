package kg.timmitof.keyboard.data.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kg.timmitof.keyboard.domain.model.KeyboardBackground
import kg.timmitof.keyboard.domain.model.PhotoCrop
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
    suspend fun import(uri: String, crop: PhotoCrop): KeyboardBackground.Photo = withContext(Dispatchers.IO) {
        val source = decode(Uri.parse(uri))
        val cropped = source.crop(crop).fitInto(OUTPUT_SIDE)
        if (cropped !== source) source.recycle()

        directory.mkdirs()
        val file = File(directory, "photo_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { cropped.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
        val tone = cropped.averageColor()
        cropped.recycle()

        directory.listFiles()?.filter { it != file }?.forEach(File::delete)
        KeyboardBackground.Photo(path = file.absolutePath, tone = tone)
    }

    private fun Bitmap.crop(crop: PhotoCrop): Bitmap {
        val rect = Rect(
            (crop.left * width).toInt().coerceIn(0, width - 1),
            (crop.top * height).toInt().coerceIn(0, height - 1),
            (crop.right * width).toInt().coerceIn(1, width),
            (crop.bottom * height).toInt().coerceIn(1, height),
        )
        if (rect.width() <= 0 || rect.height() <= 0) return this
        return Bitmap.createBitmap(this, rect.left, rect.top, rect.width(), rect.height())
    }

    private fun Bitmap.fitInto(maxSide: Int): Bitmap {
        val scale = maxSide.toFloat() / maxOf(width, height)
        if (scale >= 1f) return this
        val scaled = Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true)
        recycle()
        return scaled
    }

    /** Картинка, ужатая до одного пикселя, и есть её средний цвет. */
    private fun Bitmap.averageColor(): Long {
        val pixel = Bitmap.createScaledBitmap(this, 1, 1, true)
        val color = pixel.getPixel(0, 0)
        pixel.recycle()
        return color.toLong() and 0xFFFFFFFFL
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
        // Декодируем с запасом, чтобы приближенный кусок не был мыльным; на диск — уже уменьшенный.
        const val MAX_SIDE = 2400
        const val OUTPUT_SIDE = 1440
        const val JPEG_QUALITY = 88
    }
}
