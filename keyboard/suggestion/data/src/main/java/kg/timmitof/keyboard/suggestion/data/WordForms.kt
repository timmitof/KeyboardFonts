package kg.timmitof.keyboard.suggestion.data

import java.nio.ByteBuffer

/**
 * Фильтр Блума всех словоформ языка: «да» ошибочно примерно в 1 % случаев, «нет» — всегда точно.
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
        private const val VERSION = 1
        private const val HEADER_INTS = 5

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
    }
}
