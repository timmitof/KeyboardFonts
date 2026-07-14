package kg.timmitof.keyboard.data.emoji

import android.icu.lang.UCharacter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Поисковый индекс эмодзи по официальным Unicode-именам через ICU
 * ([UCharacter.getName], API 24+).
 */
internal object EmojiNameIndex {

    @Volatile
    private var nameByEmoji: Map<String, String>? = null

    suspend fun search(query: String): List<String> = withContext(Dispatchers.Default) {
        val normalized = query.trim().uppercase()
        if (normalized.isEmpty()) return@withContext emptyList()

        val index = nameByEmoji ?: buildIndex().also { nameByEmoji = it }
        index.filterValues { it.contains(normalized) }.keys.toList()
    }

    private fun buildIndex(): Map<String, String> =
        EmojiCatalog.categories
            .flatMap { it.emojis }
            .distinct()
            .associateWith(::unicodeName)

    private fun unicodeName(emoji: String): String =
        emoji.codePoints().toArray()
            .filterNot { it == VARIATION_SELECTOR || it == ZERO_WIDTH_JOINER }
            .joinToString(" ") { codePoint -> UCharacter.getName(codePoint).orEmpty() }

    private const val VARIATION_SELECTOR = 0xFE0F
    private const val ZERO_WIDTH_JOINER = 0x200D
}