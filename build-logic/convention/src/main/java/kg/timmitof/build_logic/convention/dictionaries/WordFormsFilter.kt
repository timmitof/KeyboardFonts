package kg.timmitof.build_logic.convention.dictionaries

import java.io.DataOutputStream
import java.io.File
import kotlin.math.ln
import kotlin.math.roundToInt

/**
 * Фильтр Блума «это настоящая словоформа»: ложных «да» — [falsePositiveRate], ложных «нет» не бывает.
 * При 3 % — около 7,3 бита на слово вместо миллионов строк: помещается в ассеты и в память IME.
 *
 * Формат файла (big-endian, как пишет [DataOutputStream]):
 * `MAGIC:Int, VERSION:Int, bits:Int, hashes:Int, words:Int`, затем `bits / 64` значений `Long`.
 *
 * Хеш — [WordHash], он же продублирован в рантайме. Менять только вместе с [VERSION].
 *
 * @param wordHashes [WordHash] всех форм без повторов: размер фильтра считается по числу различных форм.
 */
internal class WordFormsFilter(
    private val wordHashes: LongArray,
    falsePositiveRate: Double,
) {

    init {
        require(falsePositiveRate > 0.0 && falsePositiveRate < 1.0) {
            "dictionaries: доля ложных срабатываний фильтра должна быть в (0; 1), а не $falsePositiveRate"
        }
    }

    val size: Int get() = wordHashes.size

    /** Оптимум для Блума: `m/n = −ln p / ln²2` бит на слово, `k = m/n · ln 2` хешей. */
    private val bits: Int = run {
        val bitsPerWord = -ln(falsePositiveRate) / (LN_2 * LN_2)
        val wanted = Math.ceil(size.coerceAtLeast(1) * bitsPerWord).toLong()
        val rounded = (wanted + Long.SIZE_BITS - 1) / Long.SIZE_BITS * Long.SIZE_BITS
        require(rounded <= Int.MAX_VALUE) { "dictionaries: слишком много словоформ ($size) для фильтра" }
        rounded.toInt()
    }

    private val hashes: Int = (bits.toDouble() / size.coerceAtLeast(1) * LN_2).roundToInt().coerceIn(1, MAX_HASHES)

    private val words = LongArray(bits / Long.SIZE_BITS).also { words ->
        wordHashes.forEach { hash ->
            for (index in 0 until hashes) {
                val bit = bitAt(hash, index)
                words[bit ushr 6] = words[bit ushr 6] or (1L shl bit)
            }
        }
    }

    /** Размер файла в байтах. */
    val byteSize: Int get() = HEADER_BYTES + words.size * Long.SIZE_BYTES

    fun writeTo(file: File) {
        DataOutputStream(file.outputStream().buffered()).use { out ->
            out.writeInt(MAGIC)
            out.writeInt(VERSION)
            out.writeInt(bits)
            out.writeInt(hashes)
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
        const val VERSION = 2

        private const val MAX_HASHES = 16
        private const val HEADER_BYTES = 5 * Int.SIZE_BYTES
        private val LN_2 = ln(2.0)
    }
}
