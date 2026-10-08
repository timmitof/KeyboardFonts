package kg.timmitof.build_logic.convention.dictionaries

import java.io.DataOutputStream
import java.io.File

/**
 * Фильтр Блума «это настоящая словоформа»: ложных «да» около 1 %, ложных «нет» не бывает.
 * Около 10 бит на слово вместо миллионов строк — помещается в ассеты и в память IME.
 *
 * Формат файла (big-endian, как пишет [DataOutputStream]):
 * `MAGIC:Int, VERSION:Int, bits:Int, hashes:Int, words:Int`, затем `bits / 64` значений `Long`.
 *
 * Хеш — [WordHash], он же продублирован в рантайме. Менять только вместе с [VERSION].
 */
internal class WordFormsFilter(expectedWords: Int) {

    private val bits: Int = run {
        val wanted = (expectedWords.coerceAtLeast(1).toLong() * BITS_PER_WORD + Long.SIZE_BITS - 1) /
                Long.SIZE_BITS * Long.SIZE_BITS
        require(wanted <= Int.MAX_VALUE) { "dictionaries: слишком много словоформ ($expectedWords) для фильтра" }
        wanted.toInt()
    }

    private val words = LongArray(bits / Long.SIZE_BITS)

    var size: Int = 0
        private set

    /** Размер файла в байтах. */
    val byteSize: Int get() = HEADER_BYTES + words.size * Long.SIZE_BYTES

    fun add(word: String) {
        val hash = WordHash.hash(word)
        for (index in 0 until HASHES) {
            val bit = bitAt(hash, index)
            words[bit ushr 6] = words[bit ushr 6] or (1L shl bit)
        }
        size++
    }

    fun mightContain(word: String): Boolean {
        val hash = WordHash.hash(word)
        for (index in 0 until HASHES) {
            val bit = bitAt(hash, index)
            if (words[bit ushr 6] and (1L shl bit) == 0L) return false
        }
        return true
    }

    fun writeTo(file: File) {
        DataOutputStream(file.outputStream().buffered()).use { out ->
            out.writeInt(MAGIC)
            out.writeInt(VERSION)
            out.writeInt(bits)
            out.writeInt(HASHES)
            out.writeInt(size)
            words.forEach(out::writeLong)
        }
    }

    /** Двойное хеширование (Кирш — Митценмахер): `k` позиций из двух половин одного 64-битного хеша. */
    private fun bitAt(hash: Long, index: Int): Int {
        val first = hash.toInt()
        val second = (hash ushr 32).toInt() or 1
        return ((first + index * second) and Int.MAX_VALUE) % bits
    }

    companion object {
        /** `KFWF` — Keyboard Fonts Word Forms. */
        const val MAGIC = 0x4B465746
        const val VERSION = 1

        /** 10 бит на слово и 7 хешей — оптимум для ~0,8 % ложных срабатываний. */
        const val BITS_PER_WORD = 10
        const val HASHES = 7

        private const val HEADER_BYTES = 5 * Int.SIZE_BYTES
    }
}
