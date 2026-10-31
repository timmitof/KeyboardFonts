package kg.timmitof.keyboard.suggestion.data


/**
 * Фильтр Блума словоформ языка (только леммы, которые встречаются в частотном списке): «да» ошибочно
 * в заданной при сборке доле случаев (`formsFalsePositiveRate`, 3 %), «нет» — всегда точно.
 * Собирается при сборке плагином словарей (`WordFormsFilter` в build-logic), формат оттуда, хеш — [WordHash]:
 * `MAGIC, VERSION, bits, hashes, words` (Int, big-endian), затем `bits / 64` значений Long.
 *
 * Проверка — несколько чтений из массива без аллокаций: зовётся на каждое нажатие.
 */
internal class WordForms private constructor(
    private val words: LongArray,
    private val bits: Int,
    private val hashes: Int,
) {

    /** Слово в форме словаря (нижний регистр, «ё» → «е»). */
    fun mightContain(word: String): Boolean {
        if (bits == 0 || word.isEmpty()) return false

        val hash = WordHash.hash(word)
        val first = hash.toInt()
        val second = (hash ushr 32).toInt() or 1
        for (index in 0 until hashes) {
            val bit = ((first + index * second) and Int.MAX_VALUE) % bits
            if (words[bit ushr 6] and (1L shl bit) == 0L) return false
        }
        return true
    }

    companion object {

        /** Языка без списка форм: никакое слово не считается заведомо настоящим. */
        val Empty = WordForms(LongArray(0), bits = 0, hashes = 0)

        private const val MAGIC = 0x4B465746
        private const val VERSION = 2
        private const val HEADER_INTS = 5

        /** Чужой или битый файл — без фильтра, как у языка без списка форм. */
        fun parse(bytes: ByteArray): WordForms {
            if (bytes.size < HEADER_INTS * Int.SIZE_BYTES) return Empty
            if (bytes.intAt(header(0)) != MAGIC || bytes.intAt(header(1)) != VERSION) return Empty

            val bits = bytes.intAt(header(2))
            val hashes = bytes.intAt(header(3))
            // header(4) — число слов, только для логов сборки.

            val longs = bits / Long.SIZE_BITS
            if (bits <= 0 || bits % Long.SIZE_BITS != 0 || hashes <= 0) return Empty
            val wordsStart = HEADER_INTS * Int.SIZE_BYTES
            if (bytes.size - wordsStart != longs * Long.SIZE_BYTES) return Empty

            val words = LongArray(longs) { index ->
                val offset = wordsStart + index * Long.SIZE_BYTES
                (bytes.intAt(offset).toLong() shl Int.SIZE_BITS) or (bytes.intAt(offset + Int.SIZE_BYTES).toLong() and INT_MASK)
            }
            return WordForms(words, bits, hashes)
        }

        private fun header(field: Int): Int = field * Int.SIZE_BYTES

        /** Big-endian `Int` с байта [offset]. */
        private fun ByteArray.intAt(offset: Int): Int =
            ((this[offset].toInt() and 0xFF) shl 24) or ((this[offset + 1].toInt() and 0xFF) shl 16) or
                ((this[offset + 2].toInt() and 0xFF) shl 8) or (this[offset + 3].toInt() and 0xFF)

        private const val INT_MASK = 0xFFFFFFFFL
    }
}
