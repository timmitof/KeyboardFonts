package kg.timmitof.keyboard.data.font

import kg.timmitof.keyboard.domain.model.KeyboardFont

/**
 * Каталог шрифтов: строит Unicode-карты стилизации.
 */
internal object FontCatalog {

    val fonts: List<KeyboardFont> by lazy { buildFonts() }

    private fun buildFonts(): List<KeyboardFont> = listOf(
        KeyboardFont(KeyboardFont.DEFAULT_ID, emptyMap()),

        // Serif
        KeyboardFont("bold", math(upper = 0x1D400, lower = 0x1D41A, digit = 0x1D7CE)),
        KeyboardFont("italic", math(upper = 0x1D434, lower = 0x1D44E).override('h' to 0x210E)),
        KeyboardFont("bold_italic", math(upper = 0x1D468, lower = 0x1D482)),

        // Sans-serif
        KeyboardFont("sans", math(upper = 0x1D5A0, lower = 0x1D5BA, digit = 0x1D7E2)),
        KeyboardFont("sans_bold", math(upper = 0x1D5D4, lower = 0x1D5EE, digit = 0x1D7EC)),
        KeyboardFont("sans_italic", math(upper = 0x1D608, lower = 0x1D622)),
        KeyboardFont("sans_bold_italic", math(upper = 0x1D63C, lower = 0x1D656)),

        // Script
        KeyboardFont(
            "script",
            math(upper = 0x1D49C, lower = 0x1D4B6).override(
                'B' to 0x212C, 'E' to 0x2130, 'F' to 0x2131, 'H' to 0x210B, 'I' to 0x2110,
                'L' to 0x2112, 'M' to 0x2133, 'R' to 0x211B,
                'e' to 0x212F, 'g' to 0x210A, 'o' to 0x2134,
            ),
        ),
        KeyboardFont("bold_script", math(upper = 0x1D4D0, lower = 0x1D4EA)),

        // Fraktur / gothic
        KeyboardFont(
            "fraktur",
            math(upper = 0x1D504, lower = 0x1D51E).override(
                'C' to 0x212D, 'H' to 0x210C, 'I' to 0x2111, 'R' to 0x211C, 'Z' to 0x2128,
            ),
        ),
        KeyboardFont("bold_fraktur", math(upper = 0x1D56C, lower = 0x1D586)),

        // Double-struck
        KeyboardFont(
            "double_struck",
            math(upper = 0x1D538, lower = 0x1D552, digit = 0x1D7D8).override(
                'C' to 0x2102, 'H' to 0x210D, 'N' to 0x2115, 'P' to 0x2119,
                'Q' to 0x211A, 'R' to 0x211D, 'Z' to 0x2124,
            ),
        ),

        // Monospace
        KeyboardFont("monospace", math(upper = 0x1D670, lower = 0x1D68A, digit = 0x1D7F6)),

        // Прочие
        KeyboardFont("small_caps", smallCaps()),
        KeyboardFont("circled", circled()),
        KeyboardFont("squared", squared()),
        KeyboardFont("underline", combining(0x0332)),
        KeyboardFont("strikethrough", combining(0x0336)),
    )

    /**
     * Регулярное семейство: буквы идут подряд от [upper]/[lower], цифры — от [digit].
     */
    private fun math(upper: Int, lower: Int, digit: Int = ABSENT): MutableMap<Char, String> {
        val map = HashMap<Char, String>(72)
        for (i in 0..25) {
            map['A' + i] = codePoint(upper + i)
            map['a' + i] = codePoint(lower + i)
        }
        if (digit != ABSENT) for (i in 0..9) map['0' + i] = codePoint(digit + i)
        return map
    }

    /** Переопределяет отдельные буквы (для семейств с «дырами» в блоке). */
    private fun MutableMap<Char, String>.override(vararg pairs: Pair<Char, Int>): MutableMap<Char, String> {
        pairs.forEach { (char, cp) -> this[char] = codePoint(cp) }
        return this
    }

    /** Декоратор: добавляет комбинируемый знак после каждой буквы и цифры. */
    private fun combining(mark: Int): Map<Char, String> {
        val suffix = codePoint(mark)
        val map = HashMap<Char, String>(72)
        ('A'..'Z').forEach { map[it] = "$it$suffix" }
        ('a'..'z').forEach { map[it] = "$it$suffix" }
        ('0'..'9').forEach { map[it] = "$it$suffix" }
        return map
    }

    /** Малые капители: и строчные, и заглавные превращаются в small caps. */
    private fun smallCaps(): Map<Char, String> {
        // -1 — буква без small-cap формы, остаётся как есть.
        val targets = intArrayOf(
            0x1D00, 0x0299, 0x1D04, 0x1D05, 0x1D07, 0xA730, 0x0262, 0x029C, 0x026A,
            0x1D0A, 0x1D0B, 0x029F, 0x1D0D, 0x0274, 0x1D0F, 0x1D18, -1, 0x0280,
            0xA731, 0x1D1B, 0x1D1C, 0x1D20, 0x1D21, -1, 0x028F, 0x1D22,
        )
        val map = HashMap<Char, String>(64)
        for (i in 0..25) {
            val glyph = if (targets[i] == -1) ('a' + i).toString() else codePoint(targets[i])
            map['a' + i] = glyph
            map['A' + i] = glyph
        }
        return map
    }

    /** Буквы в кружках */
    private fun circled(): Map<Char, String> {
        val map = math(upper = 0x24B6, lower = 0x24D0)
        map['0'] = codePoint(0x24EA)
        for (i in 1..9) map['0' + i] = codePoint(0x2460 + (i - 1))
        return map
    }

    /** Буквы в квадратах */
    private fun squared(): Map<Char, String> {
        val map = HashMap<Char, String>(64)
        for (i in 0..25) {
            val glyph = codePoint(0x1F130 + i)
            map['A' + i] = glyph
            map['a' + i] = glyph
        }
        return map
    }

    private fun codePoint(cp: Int): String = String(Character.toChars(cp))

    private const val ABSENT = -1
}
