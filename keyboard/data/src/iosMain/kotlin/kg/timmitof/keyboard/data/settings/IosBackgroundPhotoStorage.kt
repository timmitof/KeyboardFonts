package kg.timmitof.keyboard.data.settings

import kg.timmitof.keyboard.data.settings.BackgroundPhotoStorage.StoredPhoto
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Фото приходит из системного выбора файлом (`file://`) и копируется в `Documents/backgrounds`.
 * TODO: средний цвет для оттенка (на Android — уменьшенный Bitmap) и общий с клавиатурой контейнер App Group.
 */
@OptIn(ExperimentalForeignApi::class)
class IosBackgroundPhotoStorage : BackgroundPhotoStorage {

    private val fileManager = NSFileManager.defaultManager

    private val directory: String by lazy {
        val documents = fileManager.URLForDirectory(NSDocumentDirectory, NSUserDomainMask, null, true, null)?.path
        "${requireNotNull(documents) { "Documents directory is unavailable" }}/$DIRECTORY"
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun copy(uri: String): StoredPhoto = withContext(Dispatchers.IO) {
        val source = NSURL.URLWithString(uri) ?: error("Не удалось открыть $uri")
        fileManager.createDirectoryAtPath(directory, withIntermediateDirectories = true, attributes = null, error = null)

        val extension = source.pathExtension?.takeIf { it.isNotEmpty() } ?: DEFAULT_EXTENSION
        val path = "$directory/photo_${Clock.System.now().toEpochMilliseconds()}.$extension"
        check(fileManager.copyItemAtURL(source, NSURL.fileURLWithPath(path), null)) { "Не удалось скопировать $uri" }

        StoredPhoto(path = path, tone = FALLBACK_TONE)
    }

    override suspend fun delete(path: String): Boolean = withContext(Dispatchers.IO) {
        !path.isOwned() || !fileManager.fileExistsAtPath(path) || fileManager.removeItemAtPath(path, null)
    }

    override fun discard(path: String) {
        if (path.isOwned()) fileManager.removeItemAtPath(path, null)
    }

    private fun String.isOwned(): Boolean = substringBeforeLast('/') == directory

    private companion object {
        const val DIRECTORY = "backgrounds"
        const val DEFAULT_EXTENSION = "jpg"
        const val FALLBACK_TONE = 0xFF808080L
    }
}
