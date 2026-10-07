package kg.timmitof.keyboard.suggestion.data

import java.nio.ByteBuffer

/**
 * Фильтр Блума всех словоформ языка: «да» ошибочно примерно в 1 % случаев, «нет» — всегда точно.
 * Собирается при сборке плагином словарей (`WordFormsFilter` в build-logic), формат и хеш — оттуда:
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

        val hash = hash(word)
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
        private const val VERSION = 1
        private const val HEADER_INTS = 5

        private const val FNV_OFFSET = -0x340d631b7bdddcdbL
        private const val FNV_PRIME = 0x100000001b3L
        private const val MIX_FIRST = -0xae502812aa7333L
        private const val MIX_SECOND = -0x3b314601e57a13adL

        /** Чужой или битый файл — без фильтра, как у языка без списка форм. */
        fun parse(bytes: ByteArray): WordForms {
            val buffer = ByteBuffer.wrap(bytes)
            if (buffer.remaining() < HEADER_INTS * Int.SIZE_BYTES) return Empty
            if (buffer.int != MAGIC || buffer.int != VERSION) return Empty

            val bits = buffer.int
            val hashes = buffer.int
            buffer.int // число слов — только для логов сборки

            val longs = bits / Long.SIZE_BITS
            if (bits <= 0 || bits % Long.SIZE_BITS != 0 || hashes <= 0) return Empty
            if (buffer.remaining() != longs * Long.SIZE_BYTES) return Empty

            val words = LongArray(longs)
            buffer.asLongBuffer().get(words)
            return WordForms(words, bits, hashes)
        }

        /** Копия `WordFormsFilter.hash` из build-logic: FNV-1a по байтам UTF-16 + перемешивание MurmurHash3. */
        private fun hash(word: String): Long {
            var hash = FNV_OFFSET
            for (index in word.indices) {
                val code = word[index].code
                hash = (hash xor (code and 0xFF).toLong()) * FNV_PRIME
                hash = (hash xor (code ushr 8).toLong()) * FNV_PRIME
            }
            hash = (hash xor (hash ushr 33)) * MIX_FIRST
            hash = (hash xor (hash ushr 33)) * MIX_SECOND
            return hash xor (hash ushr 33)
        }
    }
}
