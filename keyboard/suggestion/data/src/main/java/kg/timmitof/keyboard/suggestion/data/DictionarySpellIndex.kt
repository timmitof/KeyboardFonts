package kg.timmitof.keyboard.suggestion.data

import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.util.Arrays
import kotlin.math.abs

/**
 * Индекс опечаток словаря, собранный при сборке (`SpellIndexWriter` в build-logic), — symmetric delete
 * как в SymSpell, но готовый: файл не разбирается в объекты, поиск читает его как есть.
 *
 * Формат `<code>.spell` (big-endian), все поля — `Int`, если не сказано иное:
 * ```
 * MAGIC ('KFSI'), VERSION (1), words, prefixLength, maxDistance, bucketBits, postings, alphabetLength
 * alphabet: alphabetLength × Char (UTF-16), плюс нулевой Char при нечётной длине — выравнивание на 4 байта
 * offsets: (2^bucketBits + 1) × Int — начало корзины в ids; последний элемент равен postings
 * ids: postings × Int — номера строк словаря, внутри корзины по возрастанию без повторов
 * ```
 * Ключ — первые `prefixLength` букв слова и все их варианты без не более чем `maxDistance` букв;
 * корзина ключа — старшие `bucketBits` бит его [WordHash]. Сами ключи не хранятся: в корзину попадают
 * и чужие слова, поэтому каждый кандидат проверяется настоящим расстоянием.
 *
 * Все чтения — абсолютные, без изменения позиции буфера: индекс безопасно читать из нескольких потоков.
 */
internal class DictionarySpellIndex private constructor(
    private val dictionary: WordDictionary,
    private val offsets: IntBuffer,
    private val ids: IntBuffer,
    private val bucketShift: Int,
    private val prefixLength: Int,
    private val maxDistance: Int,
    private val alphabet: CharArray,
) : SpellIndex {

    override fun corrections(query: String, maxDistance: Int): List<SpellIndex.Correction> {
        if (query.isEmpty()) return emptyList()
        val distanceLimit = minOf(maxDistance, this.maxDistance)
        if (distanceLimit < 0) return emptyList()

        val candidates = IntCollector()
        WordHash.forEachDelete(query, prefixLength, distanceLimit) { hash ->
            val bucket = (hash ushr bucketShift).toInt()
            val from = offsets[bucket]
            val to = offsets[bucket + 1]
            if (from in 0..to && to <= ids.limit()) {
                for (position in from until to) candidates.add(ids[position])
            }
        }

        val distance = EditDistance()
        val text = dictionary.text
        val result = ArrayList<SpellIndex.Correction>()
        candidates.forEachUnique { id ->
            if (id !in 0 until dictionary.size) return@forEachUnique
            val length = dictionary.lengthAt(id)
            if (abs(length - query.length) > distanceLimit) return@forEachUnique

            val found = distance.between(query, text, dictionary.startAt(id), length, distanceLimit)
            if (found <= distanceLimit) {
                result += SpellIndex.Correction(dictionary.wordAt(id), found, dictionary.scoreAt(id))
            }
        }

        result.sortWith(SpellIndex.ORDER)
        return if (result.size > SpellIndex.TOP_K) result.subList(0, SpellIndex.TOP_K).toList() else result
    }

    /**
     * Индекс удалений тут не помогает — он про слова целиком. Зато словарь отсортирован: варианты [query]
     * в одну правку (удаление, перестановка, замена, вставка буквы алфавита) — это префиксы, а слова на префикс
     * лежат одним диапазоном. Диапазоны сливаются, точный префикс [query] из них вычитается.
     */
    override fun prefixCorrections(query: String, limit: Int, lengthPenalty: Int): List<SpellIndex.Correction> {
        if (query.isEmpty() || limit <= 0) return emptyList()

        val exact = dictionary.prefixRange(query)
        val ranges = RangeCollector()
        forEachPrefixNeighbor(query) { prefix -> ranges.add(dictionary.prefixRange(prefix)) }

        val top = TopIndices(limit)
        ranges.forEachMerged { index ->
            if (index in exact) return@forEachMerged
            top.offer(
                index,
                SpellIndex.prefixRank(dictionary.scoreAt(index), dictionary.lengthAt(index), query, lengthPenalty),
            )
        }

        return top.indices().map { index ->
            SpellIndex.Correction(dictionary.wordAt(index), PREFIX_DISTANCE, dictionary.scoreAt(index))
        }
    }

    /**
     * Начала в одной правке от [query]. Пропускаются заведомо лишние: замена последней буквы
     * (её покрывает удаление последней), вставка в конец (это точный префикс), повторы от двойных букв.
     */
    private inline fun forEachPrefixNeighbor(query: String, action: (String) -> Unit) {
        val length = query.length
        val builder = StringBuilder(length + 1)

        for (index in 0 until length) {
            if (index > 0 && query[index] == query[index - 1]) continue
            if (length > 1) action(builder.clear().append(query, 0, index).append(query, index + 1, length).toString())
        }
        for (index in 0 until length - 1) {
            if (query[index] == query[index + 1]) continue
            builder.clear().append(query)
            builder.setCharAt(index, query[index + 1])
            builder.setCharAt(index + 1, query[index])
            action(builder.toString())
        }
        for (index in 0 until length - 1) {
            for (char in alphabet) {
                if (char == query[index]) continue
                builder.clear().append(query)
                builder.setCharAt(index, char)
                action(builder.toString())
            }
        }
        for (index in 0 until length) {
            for (char in alphabet) {
                if (char == query[index]) continue
                action(builder.clear().append(query, 0, index).append(char).append(query, index, length).toString())
            }
        }
    }

    /** Номера кандидатов из корзин; одинаковые схлопываются сортировкой — их обычно пара сотен. */
    private class IntCollector {
        private var values = IntArray(INITIAL_CAPACITY)
        private var size = 0

        fun add(value: Int) {
            if (size == values.size) values = values.copyOf(size * 2)
            values[size++] = value
        }

        inline fun forEachUnique(action: (Int) -> Unit) {
            Arrays.sort(values, 0, size)
            for (index in 0 until size) {
                if (index > 0 && values[index] == values[index - 1]) continue
                action(values[index])
            }
        }

        private companion object {
            const val INITIAL_CAPACITY = 256
        }
    }

    /** Диапазоны словаря: упакованы в `Long` (начало, конец), сортируются и сливаются при обходе. */
    private class RangeCollector {
        private var ranges = LongArray(INITIAL_CAPACITY)
        private var size = 0

        fun add(range: IntRange) {
            if (range.isEmpty()) return
            if (size == ranges.size) ranges = ranges.copyOf(size * 2)
            ranges[size++] = (range.first.toLong() shl Int.SIZE_BITS) or (range.last + 1).toLong()
        }

        inline fun forEachMerged(action: (Int) -> Unit) {
            Arrays.sort(ranges, 0, size)
            var next = 0
            for (index in 0 until size) {
                val start = maxOf((ranges[index] ushr Int.SIZE_BITS).toInt(), next)
                val end = ranges[index].toInt()
                for (word in start until end) action(word)
                if (end > next) next = end
            }
        }

        private companion object {
            const val INITIAL_CAPACITY = 64
        }
    }

    companion object {

        private const val MAGIC = 0x4B465349
        private const val VERSION = 1
        private const val HEADER_INTS = 8
        private const val MAX_BUCKET_BITS = 30

        private const val PREFIX_DISTANCE = 1

        /**
         * Индекс поверх [buffer] без копирования; `null` — чужой, битый или собранный для другого словаря файл
         * (номера слов тогда указывали бы не туда).
         */
        fun open(buffer: ByteBuffer, dictionary: WordDictionary): DictionarySpellIndex? {
            val bytes = buffer.duplicate().order(ByteOrder.BIG_ENDIAN)
            val base = bytes.position()
            val available = bytes.remaining().toLong()
            if (available < HEADER_INTS * Int.SIZE_BYTES) return null

            fun header(field: Int): Int = bytes.getInt(base + field * Int.SIZE_BYTES)

            if (header(0) != MAGIC || header(1) != VERSION) return null
            val words = header(2)
            val prefixLength = header(3)
            val maxDistance = header(4)
            val bucketBits = header(5)
            val postings = header(6)
            val alphabetLength = header(7)

            if (words != dictionary.size) return null
            if (prefixLength <= 0 || maxDistance < 0 || postings < 0 || alphabetLength < 0) return null
            if (bucketBits !in 1..MAX_BUCKET_BITS) return null

            val alphabetStart = base + HEADER_INTS * Int.SIZE_BYTES
            val alphabetChars = alphabetLength + alphabetLength % 2
            val offsetsStart = alphabetStart.toLong() + alphabetChars * Char.SIZE_BYTES
            val buckets = 1L shl bucketBits
            val idsStart = offsetsStart + (buckets + 1) * Int.SIZE_BYTES
            val end = idsStart + postings.toLong() * Int.SIZE_BYTES
            if (end - base != available) return null

            val alphabet = CharArray(alphabetLength) { bytes.getChar(alphabetStart + it * Char.SIZE_BYTES) }
            val offsets = bytes.intView(offsetsStart.toInt(), (buckets + 1).toInt())
            if (offsets[buckets.toInt()] != postings) return null

            return DictionarySpellIndex(
                dictionary = dictionary,
                offsets = offsets,
                ids = bytes.intView(idsStart.toInt(), postings),
                bucketShift = Long.SIZE_BITS - bucketBits,
                prefixLength = prefixLength,
                maxDistance = maxDistance,
                alphabet = alphabet,
            )
        }

        /** Окно буфера как `Int`-массив. `position`/`limit` через [Buffer] — старые API возвращают его. */
        private fun ByteBuffer.intView(start: Int, count: Int): IntBuffer {
            val window = duplicate()
            (window as Buffer).limit(start + count * Int.SIZE_BYTES)
            (window as Buffer).position(start)
            return window.slice().order(ByteOrder.BIG_ENDIAN).asIntBuffer()
        }
    }
}
