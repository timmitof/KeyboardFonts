package kg.timmitof.keyboard.data.font

import kg.timmitof.keyboard.font.domain.model.FontScript
import kg.timmitof.keyboard.font.domain.model.KeyboardFont
import kg.timmitof.keyboard.font.domain.model.StyledText

object FontCatalog {

    val fonts: List<KeyboardFont> by lazy { buildFonts() }

    private fun buildFonts(): List<KeyboardFont> = listOf(
        KeyboardFont(KeyboardFont.DEFAULT_ID, emptyMap()),

        latin(
            "script",
            math(upper = 0x1D49C, lower = 0x1D4B6).override(
                'B' to 0x212C, 'E' to 0x2130, 'F' to 0x2131, 'H' to 0x210B, 'I' to 0x2110,
                'L' to 0x2112, 'M' to 0x2133, 'R' to 0x211B,
                'e' to 0x212F, 'g' to 0x210A, 'o' to 0x2134,
            ),
        ),
        latin("bold_script", math(upper = 0x1D4D0, lower = 0x1D4EA)),

        latin(
            "fraktur",
            math(upper = 0x1D504, lower = 0x1D51E).override(
                'C' to 0x212D, 'H' to 0x210C, 'I' to 0x2111, 'R' to 0x211C, 'Z' to 0x2128,
            ),
        ),
        latin("bold_fraktur", math(upper = 0x1D56C, lower = 0x1D586)),

        latin(
            "double_struck",
            math(upper = 0x1D538, lower = 0x1D552, digit = 0x1D7D8).override(
                'C' to 0x2102, 'H' to 0x210D, 'N' to 0x2115, 'P' to 0x2119,
                'Q' to 0x211A, 'R' to 0x211D, 'Z' to 0x2124,
            ),
        ),

        latin("circled", circled()),
        latin("squared", squared()),
        latin("small_caps", smallCaps()),

        latin("bold", math(upper = 0x1D400, lower = 0x1D41A, digit = 0x1D7CE)),
        latin("italic", math(upper = 0x1D434, lower = 0x1D44E).override('h' to 0x210E)),
        latin("bold_italic", math(upper = 0x1D468, lower = 0x1D482)),

        latin("sans", math(upper = 0x1D5A0, lower = 0x1D5BA, digit = 0x1D7E2)),
        latin("sans_bold", math(upper = 0x1D5D4, lower = 0x1D5EE, digit = 0x1D7EC)),
        latin("sans_italic", math(upper = 0x1D608, lower = 0x1D622)),
        latin("sans_bold_italic", math(upper = 0x1D63C, lower = 0x1D656)),

        latin("monospace", math(upper = 0x1D670, lower = 0x1D68A, digit = 0x1D7F6)),

        marks("underline", 0x0332),
        marks("strikethrough", 0x0336),
        marks("double_underline", 0x0333),
        marks("slashed", 0x0338),
        marks("clouds", 0x035C, 0x0361),
        marks("arc", 0x0361),
        marks("dot_above", 0x0307),
        marks("tilde_above", 0x0303),

        KeyboardFont("spaced_caps", spacedCaps()),
        KeyboardFont("old_slavonic", oldSlavonic(), scripts = CyrillicOnly),
    )

    private fun latin(id: String, charMap: Map<Char, String>): KeyboardFont =
        KeyboardFont(id, charMap, scripts = LatinOnly)

    /** Знаки ставятся после каждой буквы и цифры любого алфавита — таблица на каждую букву не нужна. */
    private fun marks(id: String, vararg marks: Int): KeyboardFont =
        KeyboardFont(id, emptyMap(), marks = marks.joinToString("") { codePoint(it) })

    private fun math(upper: Int, lower: Int, digit: Int = ABSENT): MutableMap<Char, String> {
        val map = HashMap<Char, String>(72)
        for (i in 0..25) {
            map['A' + i] = codePoint(upper + i)
            map['a' + i] = codePoint(lower + i)
        }
        if (digit != ABSENT) for (i in 0..9) map['0' + i] = codePoint(digit + i)
        return map
    }

    private fun MutableMap<Char, String>.override(vararg pairs: Pair<Char, Int>): MutableMap<Char, String> {
        pairs.forEach { (char, cp) -> this[char] = codePoint(cp) }
        return this
    }

    /** Заглавные с узким неразрывным пробелом после каждой буквы и цифры: «П Р И В Е Т». */
    private fun spacedCaps(): Map<Char, String> {
        val map = HashMap<Char, String>(160)
        val letters = ('A'..'Z') + ('a'..'z') + ('А'..'я') + 'Ё' + 'ё' + ('0'..'9')
        letters.forEach { map[it] = "${it.uppercaseChar()}${StyledText.LETTER_SPACING}" }
        return map
    }

    /**
     * Старославянский вид: исторические буквы из блоков Cyrillic (U+0460–U+047F) и Cyrillic Extended-B
     * (U+A640–U+A69F) там, где у современной буквы есть прямой предок. Остальные буквы не меняются.
     */
    private fun oldSlavonic(): Map<Char, String> {
        val map = HashMap<Char, String>(32)
        fun letter(plain: Char, upper: Int, lower: Int) {
            map[plain.uppercaseChar()] = codePoint(upper)
            map[plain] = codePoint(lower)
        }
        letter('е', upper = 0x0462, lower = 0x0463) // ѣ ять
        letter('з', upper = 0xA640, lower = 0xA641) // ꙁ зело
        letter('о', upper = 0x047A, lower = 0x047B) // ѻ широкое о
        letter('у', upper = 0xA64A, lower = 0xA64B) // ꙋ ук
        letter('ф', upper = 0x0472, lower = 0x0473) // ѳ фита
        letter('ъ', upper = 0xA64E, lower = 0xA64F) // ꙏ нейтральный ер
        letter('ы', upper = 0xA650, lower = 0xA651) // ꙑ еры
        letter('я', upper = 0xA656, lower = 0xA657) // ꙗ йотированный аз
        return map
    }

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

    private fun circled(): Map<Char, String> {
        val map = math(upper = 0x24B6, lower = 0x24D0)
        map['0'] = codePoint(0x24EA)
        for (i in 1..9) map['0' + i] = codePoint(0x2460 + (i - 1))
        return map
    }

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

    private val LatinOnly = setOf(FontScript.LATIN)
    private val CyrillicOnly = setOf(FontScript.CYRILLIC)
}
