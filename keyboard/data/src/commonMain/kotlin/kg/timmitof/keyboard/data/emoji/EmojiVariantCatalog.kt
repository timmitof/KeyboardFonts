package kg.timmitof.keyboard.data.emoji

import kg.timmitof.keyboard.font.domain.text.codePointAtIndex
import kg.timmitof.keyboard.font.domain.text.codePointList
import kg.timmitof.keyboard.font.domain.text.codePointToString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.concurrent.Volatile

internal object EmojiVariantCatalog {

    @Volatile
    private var variantsByBase: Map<String, List<String>>? = null

    private val mutex = Mutex()

    suspend fun getVariants(): Map<String, List<String>> = variantsByBase ?: mutex.withLock {
        variantsByBase ?: withContext(Dispatchers.Default) { buildVariants() }.also { variantsByBase = it }
    }

    private suspend fun buildVariants(): Map<String, List<String>> {
        val glyphs = GlyphProbe()
        return EmojiCatalog.getCategories()
            .asSequence()
            .flatMap { it.emojis }
            .distinct()
            .filter { it.isModifierBase() }
            .mapNotNull { base ->
                // Достаточно проверить один тон: шрифты поставляются с полным набором
                val probe = base.withSkinTone(SKIN_TONES.first())
                if (!glyphs.hasGlyph(probe)) return@mapNotNull null

                base to (listOf(base) + SKIN_TONES.map { tone -> base.withSkinTone(tone) })
            }
            .toMap()
    }

    /** Тон кожи бывает только у Emoji_Modifier_Base — остальным hasGlyph не нужен. */
    private fun String.isModifierBase(): Boolean = isEmojiModifierBase(codePointAtIndex(0))

    private fun String.withSkinTone(tone: Int): String {
        val codePoints = codePointList()
        val builder = StringBuilder(length + 2)
        builder.append(codePointToString(codePoints.first()))
        builder.append(codePointToString(tone))
        codePoints.drop(1)
            .dropWhile { it == VARIATION_SELECTOR }
            .forEach { builder.append(codePointToString(it)) }
        return builder.toString()
    }

    private val SKIN_TONES = listOf(0x1F3FB, 0x1F3FC, 0x1F3FD, 0x1F3FE, 0x1F3FF)

    private const val VARIATION_SELECTOR = 0xFE0F
}
