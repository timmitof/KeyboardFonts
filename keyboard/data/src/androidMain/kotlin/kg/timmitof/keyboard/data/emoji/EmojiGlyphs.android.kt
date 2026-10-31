package kg.timmitof.keyboard.data.emoji

import android.graphics.Paint
import android.icu.lang.UCharacter
import android.icu.lang.UProperty
import android.os.Build

internal actual class GlyphProbe actual constructor() {

    private val paint = Paint()

    actual fun hasGlyph(text: String): Boolean = paint.hasGlyph(text)
}

/** На API < 28 ICU не знает свойство, там остаётся проверка глифа. */
internal actual fun isEmojiModifierBase(codePoint: Int): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return true
    return UCharacter.hasBinaryProperty(codePoint, UProperty.EMOJI_MODIFIER_BASE)
}
