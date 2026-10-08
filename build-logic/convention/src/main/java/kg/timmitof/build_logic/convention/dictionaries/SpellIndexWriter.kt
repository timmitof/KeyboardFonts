package kg.timmitof.build_logic.convention.dictionaries

import java.io.DataOutputStream
import java.io.File

/**
 * Индекс опечаток `<code>.spell` — symmetric delete, как в SymSpell, но собранный заранее:
 * для каждого слова берутся первые [PREFIX_LENGTH] букв и все их варианты без не более чем
 * [MAX_DISTANCE] букв. Вариант хранится не строкой, а корзиной по своему хешу ([WordHash]); в корзине —
 * номера строк словаря `<code>.dict`. Разные варианты могут попасть в одну корзину — рантайм всё равно
 * проверяет каждого кандидата настоящим расстоянием, так что коллизия стоит лишней проверки, а не ошибки.
 *
 * Формат (big-endian, как пишет [DataOutputStream]), все поля — `Int`, если не сказано иное:
 * ```
 * MAGIC, VERSION, words, prefixLength, maxDistance, bucketBits, postings, alphabetLength
 * alphabet: alphabetLength × Char (UTF-16), плюс один нулевой Char, если длина нечётная (выравнивание на 4)
 * offsets: (2^bucketBits + 1) × Int — начало корзины в ids, последний элемент равен postings
 * ids: postings × Int — номера слов, внутри корзины по возрастанию и без повторов
 * ```
 * Корзина варианта — старшие `bucketBits` бит его хеша. Алфавит нужен поиску по префиксу с правкой:
 * он перебирает замены и вставки букв. Рантайм — `keyboard/suggestion/data/.../DictionarySpellIndex.kt`;
 * менять формат только вместе с [VERSION].
 *
 * @param words слова в порядке строк словаря: номер слова в индексе — номер его строки.
 */
internal class SpellIndexWriter(
    private val words: List<String>,
    private val alphabet: String,
) {

    class Stats(val postings: Int, val buckets: Int, val bytes: Long)

    fun writeTo(file: File): Stats {
        val bucketBits = bucketBitsFor(words.size)
        val buckets = 1 shl bucketBits
        val shift = Long.SIZE_BITS - bucketBits

        // Два прохода вместо списков на корзину: сначала размеры, потом заполнение готового массива.
        val offsets = IntArray(buckets + 1)
        val wordBuckets = IntArray(MAX_KEYS_PER_WORD)
        words.forEach { word ->
            val count = word.collectBuckets(shift, wordBuckets)
            for (index in 0 until count) offsets[wordBuckets[index] + 1]++
        }
        for (bucket in 0 until buckets) offsets[bucket + 1] += offsets[bucket]

        val postings = offsets[buckets]
        val ids = IntArray(postings)
        val filled = offsets.copyOf(buckets)
        // Слова идут по порядку — номера в корзине получаются отсортированными.
        words.forEachIndexed { id, word ->
            val count = word.collectBuckets(shift, wordBuckets)
            for (index in 0 until count) ids[filled[wordBuckets[index]]++] = id
        }

        DataOutputStream(file.outputStream().buffered(BUFFER_SIZE)).use { out ->
            out.writeInt(MAGIC)
            out.writeInt(VERSION)
            out.writeInt(words.size)
            out.writeInt(PREFIX_LENGTH)
            out.writeInt(MAX_DISTANCE)
            out.writeInt(bucketBits)
            out.writeInt(postings)
            out.writeInt(alphabet.length)
            alphabet.forEach { out.writeChar(it.code) }
            if (alphabet.length % 2 != 0) out.writeChar(0)
            offsets.forEach(out::writeInt)
            ids.forEach(out::writeInt)
        }

        return Stats(postings = postings, buckets = buckets, bytes = file.length())
    }

    /**
     * Корзины всех вариантов слова без повторов (двойные буквы дают одинаковые варианты).
     * Пустой вариант не индексируется: он совпал бы с любым коротким словом — так же делает SymSpell.
     */
    private fun String.collectBuckets(shift: Int, into: IntArray): Int {
        val length = minOf(this.length, PREFIX_LENGTH)
        var count = 0

        fun add(hash: Long) {
            val bucket = (hash ushr shift).toInt()
            for (index in 0 until count) if (into[index] == bucket) return
            into[count++] = bucket
        }

        add(WordHash.hash(this, length))
        if (length > 1) {
            for (first in 0 until length) {
                add(WordHash.hash(this, length, first))
            }
        }
        if (MAX_DISTANCE >= 2 && length > 2) {
            for (first in 0 until length) {
                for (second in first + 1 until length) add(WordHash.hash(this, length, first, second))
            }
        }
        return count
    }

    /** Примерно 4 корзины на слово: при ~25 вариантах на слово в корзине в среднем 4–5 номеров. */
    private fun bucketBitsFor(words: Int): Int {
        var bits = MIN_BUCKET_BITS
        while (bits < MAX_BUCKET_BITS && (1L shl bits) < words.toLong() * BUCKETS_PER_WORD) bits++
        return bits
    }

    companion object {
        /** `KFSI` — Keyboard Fonts Spell Index. */
        const val MAGIC = 0x4B465349
        const val VERSION = 1

        /** Как `prefixLength` в SymSpell: опечатки дальше седьмой буквы проверяются уже расстоянием. */
        const val PREFIX_LENGTH = 7
        const val MAX_DISTANCE = 2

        private const val BUCKETS_PER_WORD = 4
        private const val MIN_BUCKET_BITS = 4
        private const val MAX_BUCKET_BITS = 24

        /** Само слово, 7 вариантов без одной буквы и 21 — без двух. */
        private const val MAX_KEYS_PER_WORD = 1 + PREFIX_LENGTH + PREFIX_LENGTH * (PREFIX_LENGTH - 1) / 2

        private const val BUFFER_SIZE = 1 shl 16
    }
}
