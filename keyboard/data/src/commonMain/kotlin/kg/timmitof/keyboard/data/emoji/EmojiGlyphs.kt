package kg.timmitof.keyboard.data.emoji

/** Умеет ли системный шрифт нарисовать символ: старые системы не знают часть эмодзи и рисуют пустые квадраты. */
internal expect class GlyphProbe() {

    fun hasGlyph(text: String): Boolean
}

/** Тон кожи бывает только у символов со свойством Unicode `Emoji_Modifier_Base`. */
internal expect fun isEmojiModifierBase(codePoint: Int): Boolean
