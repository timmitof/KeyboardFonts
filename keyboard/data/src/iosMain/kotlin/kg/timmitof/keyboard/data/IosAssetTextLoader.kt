package kg.timmitof.keyboard.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.stringWithContentsOfFile
import platform.posix.memcpy

/** Ассеты лежат в бандле в каталоге `assets/` под теми же путями, что у Android. */
@OptIn(ExperimentalForeignApi::class)
class IosAssetTextLoader : AssetTextLoader {

    override suspend fun loadText(path: String): String? = withContext(Dispatchers.IO) {
        resourcePath(path)?.let { NSString.stringWithContentsOfFile(it, NSUTF8StringEncoding, null) }
    }

    override suspend fun loadBytes(path: String): ByteArray? = withContext(Dispatchers.IO) {
        val data = resourcePath(path)?.let { NSData.dataWithContentsOfFile(it) } ?: return@withContext null
        ByteArray(data.length.toInt()).apply {
            if (isNotEmpty()) usePinned { memcpy(it.addressOf(0), data.bytes, data.length) }
        }
    }

    private fun resourcePath(path: String): String? {
        val bundle = NSBundle.mainBundle.resourcePath ?: return null
        return "$bundle/$ASSETS_DIRECTORY/$path".takeIf { NSFileManager.defaultManager.fileExistsAtPath(it) }
    }

    private companion object {
        const val ASSETS_DIRECTORY = "assets"
    }
}
