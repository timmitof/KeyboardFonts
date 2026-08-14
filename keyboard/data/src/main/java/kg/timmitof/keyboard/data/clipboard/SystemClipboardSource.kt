package kg.timmitof.keyboard.data.clipboard

import android.content.ClipboardManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Системный буфер обмена
 */
@Singleton
class SystemClipboardSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private val clipboardManager: ClipboardManager?
        get() = context.getSystemService(ClipboardManager::class.java)

    fun read(): String? = runCatching {
        val clip = clipboardManager?.primaryClip ?: return null

        (0 until clip.itemCount)
            .firstNotNullOfOrNull { clip.getItemAt(it).coerceToText(context)?.toString() }
            ?.trim()
            ?.takeIf { it.isNotEmpty() && it.length <= MAX_TEXT_LENGTH }
    }.getOrNull()

    private companion object {
        const val MAX_TEXT_LENGTH = 2_000
    }
}
