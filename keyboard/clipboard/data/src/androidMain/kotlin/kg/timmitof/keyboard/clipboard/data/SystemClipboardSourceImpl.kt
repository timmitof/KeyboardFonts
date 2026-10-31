package kg.timmitof.keyboard.clipboard.data

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build

class SystemClipboardSourceImpl(
    private val context: Context,
) : SystemClipboardSource {

    private val clipboardManager: ClipboardManager?
        get() = context.getSystemService(ClipboardManager::class.java)

    override fun read(): String? = runCatching {
        val clip = clipboardManager?.primaryClip ?: return null
        if (clip.isSensitive()) return null

        (0 until clip.itemCount)
            .firstNotNullOfOrNull { clip.getItemAt(it).coerceToText(context)?.toString() }
            ?.toClipText()
    }.getOrNull()

    /** Пароли и коды из менеджеров паролей помечены системой — их в историю не берём. */
    private fun android.content.ClipData.isSensitive(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            description.extras?.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE, false) == true
}
