package kg.timmitof.keyboard.data.emoji

import android.graphics.Paint
import android.icu.lang.UCharacter
import android.icu.lang.UProperty
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal object EmojiVariantCatalog {

    @Volatile
    private var variantsByBase: Map<String, List<String>>? = null

    private val mutex = Mutex()

    suspend fun getVariants(): Map<String, List<String>> = variantsByBase ?: mutex.withLock {
        variantsByBase ?: withContext(Dispatchers.Default) { buildVariants() }.also { variantsByBase = it }
    }

    private suspend fun buildVariants(): Map<String, List<String>> {
        val paint = Paint()
        return EmojiCatalog.getCategories()
            .asSequence()
            .flatMap { it.emojis }
            .distinct()
            .filter { it.isModifierBase() }
            .mapNotNull { base ->
                // Достаточно проверить один тон: шрифты поставляются с полным набором
                val probe = base.withSkinTone(SKIN_TONES.first())
                if (!paint.hasGlyph(probe)) return@mapNotNull null

                base to (listOf(base) + SKIN_TONES.map { tone -> base.withSkinTone(tone) })
            }
            .toMap()
    }

    /**
     * Тон кожи бывает только у Emoji_Modifier_Base — остальным hasGlyph не нужен.
     * На API < 28 ICU не знает свойство, там остаётся проверка глифа.
     */
    private fun String.isModifierBase(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return true
        return UCharacter.hasBinaryProperty(codePointAt(0), UProperty.EMOJI_MODIFIER_BASE)
    }

    private fun String.withSkinTone(tone: Int): String {
        val codePoints = codePoints().toArray()
        val builder = StringBuilder(length + 2)
        builder.appendCodePoint(codePoints.first())
        builder.appendCodePoint(tone)
        codePoints.drop(1)
            .dropWhile { it == VARIATION_SELECTOR }
            .forEach { builder.appendCodePoint(it) }
        return builder.toString()
    }

    private val SKIN_TONES = listOf(0x1F3FB, 0x1F3FC, 0x1F3FD, 0x1F3FE, 0x1F3FF)

    private const val VARIATION_SELECTOR = 0xFE0F
}
