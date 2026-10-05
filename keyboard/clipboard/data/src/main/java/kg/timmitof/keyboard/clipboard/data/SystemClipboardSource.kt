package kg.timmitof.keyboard.clipboard.data

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemClipboardSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private val clipboardManager: ClipboardManager?
        get() = context.getSystemService(ClipboardManager::class.java)

    fun read(): String? = runCatching {
        val clip = clipboardManager?.primaryClip ?: return null
        if (clip.isSensitive()) return null

        (0 until clip.itemCount)
            .firstNotNullOfOrNull { clip.getItemAt(it).coerceToText(context)?.toString() }
            ?.trim()
            ?.takeIf { it.isNotEmpty() && it.length <= MAX_TEXT_LENGTH }
    }.getOrNull()

    /** Пароли и коды из менеджеров паролей помечены системой — их в историю не берём. */
    private fun android.content.ClipData.isSensitive(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            description.extras?.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE, false) == true

    private companion object {
        const val MAX_TEXT_LENGTH = 2_000
    }
}
