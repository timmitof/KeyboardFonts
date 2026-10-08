package kg.timmitof.keyboard.suggestion.data

import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.util.Arrays
import kotlin.math.abs

/**
 * Индекс опечаток словаря, собранный при сборке (`SpellIndexWriter` в build-logic), — symmetric delete
 * как в SymSpell, но готовый и только на одну правку: файл не разбирается в объекты, поиск читает его как есть.
 *
 * Формат `<code>.spell` (big-endian), все поля — `Int`, если не сказано иное:
 * ```
 * MAGIC ('KFSI'), VERSION (2), words, groups, prefixLength, indexedDeletes (1), bucketBits, entries, postingsBytes,
 * alphabetLength
 * alphabet: alphabetLength × Char (UTF-16), плюс нулевой Char при нечётной длине — выравнивание на 4 байта
 * offsets: (2^bucketBits + 1) × Int — начало корзины в postings в байтах; последний элемент равен postingsBytes
 * postings: записи корзин подряд; запись = varint(группа − предыдущая группа корзины), байт отпечатка
 * ```
 * Индексируются не слова, а группы — слова с одинаковым началом (первые `prefixLength` букв), они в словаре
 * идут подряд. Ключи группы — её начало и все варианты без одной буквы; корзина ключа — старшие `bucketBits` бит
 * его [WordHash], отпечаток — младший байт. Чужой ключ с тем же отпечатком попадается редко,
 * а каждый кандидат всё равно проверяется настоящим расстоянием.
 *
 * Все чтения — абсолютные, без изменения позиции буфера: индекс безопасно читать из нескольких потоков.
 */
internal class DictionarySpellIndex private constructor(
    private val dictionary: WordDictionary,
    private val bytes: ByteBuffer,
    private val offsets: IntBuffer,
    private val postingsStart: Int,
    private val postingsBytes: Int,
    private val groupStarts: IntArray,
    private val bucketShift: Int,
    private val prefixLength: Int,
    private val alphabet: CharArray,
) : SpellIndex {

    private val groupCount: Int get() = groupStarts.size - 1

    override fun corrections(query: String, maxDistance: Int): List<SpellIndex.Correction> {
        if (query.isEmpty()) return emptyList()
        val distanceLimit = minOf(maxDistance, SpellIndex.MAX_DISTANCE)
        if (distanceLimit < 0) return emptyList()

        val groups = IntCollector()
        forEachQueryKey(query, distanceLimit) { hash -> collect(hash, groups) }

        val distance = EditDistance()
        val text = dictionary.text
        val result = ArrayList<SpellIndex.Correction>()
        groups.forEachUnique { group ->
            if (group !in 0 until groupCount) return@forEachUnique
            for (id in groupStarts[group] until groupStarts[group + 1]) {
                val length = dictionary.lengthAt(id)
                if (abs(length - query.length) > distanceLimit) continue

                val found = distance.between(query, text, dictionary.startAt(id), length, distanceLimit)
                if (found <= distanceLimit) {
                    result += SpellIndex.Correction(dictionary.wordAt(id), found, dictionary.scoreAt(id))
                }
            }
        }

        result.sortWith(SpellIndex.ORDER)
        return if (result.size > SpellIndex.TOP_K) result.subList(0, SpellIndex.TOP_K).toList() else result
    }

    /**
     * Ключи запроса, которые вместе с ключами словаря «без одной буквы» находят все слова в [distanceLimit] правок.
     *
     * Symmetric delete на расстоянии 2 требует удалить до двух букв с обеих сторон, а в индексе — максимум одна.
     * Правки, которые съедают удаление со стороны слова, — замена, пропуск и перестановка букв. Если таких две,
     * одну исправляем прямо в запросе (перебор вариантов), а оставшуюся находит обычный поиск на одну правку.
     */
    private inline fun forEachQueryKey(query: String, distanceLimit: Int, action: (Long) -> Unit) {
        // Запрос без ≤ distanceLimit букв против слова без ≤ 1: покрывает лишние буквы в запросе
        // и не больше одной правки со стороны слова.
        WordHash.forEachDelete(query, prefixLength, distanceLimit, action)
        if (distanceLimit < 2) return

        // Две лишние буквы в начале запроса сдвигают обрезку: от начала слова ушли бы две последние буквы,
        // а в индексе — максимум одна. Поэтому две буквы удаляются из начала длиной prefixLength + 2.
        if (query.length > prefixLength) {
            val length = minOf(query.length, prefixLength + 2)
            for (first in 0 until length) {
                for (second in first + 1 until length) action(WordHash.hash(query, length, first, second))
            }
        }

        forEachWordSideFix(query) { variant, skipFirst, skipSecond ->
            val length = minOf(variant.length, prefixLength)
            action(WordHash.hash(variant, length))
            if (length > 1) {
                for (skip in 0 until length) {
                    if (skip != skipFirst && skip != skipSecond) action(WordHash.hash(variant, length, skip))
                }
            }
        }
    }

    /**
     * Запрос с одной исправленной правкой на стороне слова в пределах начала: замена буквы, вставка пропущенной,
     * перестановка соседних. Правки дальше начала на ключи не влияют, поэтому вариант строится только из начала.
     * [action] получает вариант и позиции, удаление которых даёт ключ, уже проверенный без исправления
     * (`-1` — такой нет): замена на месте `i` минус `i` — это запрос минус `i`.
     */
    private inline fun forEachWordSideFix(query: String, action: (CharSequence, Int, Int) -> Unit) {
        val prefix = minOf(query.length, prefixLength)
        val variant = StringBuilder(prefix + 1)

        for (index in 0 until prefix) {
            val original = query[index]
            for (char in alphabet) {
                if (char == original) continue
                variant.setLength(0)
                variant.append(query, 0, prefix).setCharAt(index, char)
                action(variant, index, -1)
            }
        }

        // Вставка за началом его не меняет; у короткого запроса можно вставить и в конец.
        // Буква, равная предыдущей, даёт тот же вариант, что и вставка на шаг левее.
        val insertions = if (query.length >= prefixLength) prefixLength else query.length + 1
        for (index in 0 until insertions) {
            for (char in alphabet) {
                if (index > 0 && char == query[index - 1]) continue
                variant.setLength(0)
                variant.append(query, 0, prefix).insert(index, char)
                action(variant, index, -1)
            }
        }

        for (index in 0 until prefix - 1) {
            if (query[index] == query[index + 1]) continue
            variant.setLength(0)
            variant.append(query, 0, prefix)
            variant.setCharAt(index, query[index + 1])
            variant.setCharAt(index + 1, query[index])
            action(variant, index, index + 1)
        }
    }

    /** Группы корзины ключа с совпавшим отпечатком; битая корзина пропускается. */
    private fun collect(hash: Long, into: IntCollector) {
        val bucket = (hash ushr bucketShift).toInt()
        val fingerprint = (hash and FINGERPRINT_MASK).toInt()
        var position = offsets[bucket]
        val end = offsets[bucket + 1]
        if (position < 0 || position > end || end > postingsBytes) return

        var group = 0
        while (position < end) {
            var delta = 0
            var shift = 0
            while (true) {
                if (position >= end || shift > VARINT_MAX_SHIFT) return
                val byte = bytes.get(postingsStart + position++).toInt()
                delta = delta or ((byte and VARINT_PAYLOAD) shl shift)
                if (byte and VARINT_CONTINUATION == 0) break
                shift += VARINT_BITS
            }
            if (position >= end) return
            val entryFingerprint = bytes.get(postingsStart + position++).toInt() and FINGERPRINT_BYTE
            group += delta
            if (entryFingerprint == fingerprint) into.add(group)
        }
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

    /** Номера групп из корзин; одинаковые схлопываются сортировкой. */
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
        private const val VERSION = 2
        private const val HEADER_INTS = 10
        private const val MAX_BUCKET_BITS = 30

        /** Поиск выше рассчитан на ключи словаря ровно без одной буквы. */
        private const val INDEXED_DELETES = 1

        private const val FINGERPRINT_MASK = 0xFFL
        private const val FINGERPRINT_BYTE = 0xFF

        private const val VARINT_BITS = 7
        private const val VARINT_PAYLOAD = 0x7F
        private const val VARINT_CONTINUATION = 0x80

        /** Пять байт varint покрывают `Int`; дальше — битый файл. */
        private const val VARINT_MAX_SHIFT = 28

        private const val PREFIX_DISTANCE = 1

        /**
         * Индекс поверх [buffer] без копирования; `null` — чужой, битый или собранный для другого словаря файл
         * (номера групп тогда указывали бы не туда).
         */
        fun open(buffer: ByteBuffer, dictionary: WordDictionary): DictionarySpellIndex? {
            val bytes = buffer.duplicate().order(ByteOrder.BIG_ENDIAN)
            val base = bytes.position()
            val available = bytes.remaining().toLong()
            if (available < HEADER_INTS * Int.SIZE_BYTES) return null

            fun header(field: Int): Int = bytes.getInt(base + field * Int.SIZE_BYTES)

            if (header(0) != MAGIC || header(1) != VERSION) return null
            val words = header(2)
            val groups = header(3)
            val prefixLength = header(4)
            val indexedDeletes = header(5)
            val bucketBits = header(6)
            // header(7) — число записей, только для логов сборки.
            val postingsBytes = header(8)
            val alphabetLength = header(9)

            if (words != dictionary.size) return null
            if (prefixLength <= 0 || indexedDeletes != INDEXED_DELETES) return null
            if (groups < 0 || postingsBytes < 0 || alphabetLength < 0) return null
            if (bucketBits !in 1..MAX_BUCKET_BITS) return null

            val alphabetStart = base + HEADER_INTS * Int.SIZE_BYTES
            val alphabetChars = alphabetLength + alphabetLength % 2
            val offsetsStart = alphabetStart.toLong() + alphabetChars * Char.SIZE_BYTES
            val buckets = 1L shl bucketBits
            val postingsStart = offsetsStart + (buckets + 1) * Int.SIZE_BYTES
            val end = postingsStart + postingsBytes
            if (end - base != available) return null

            val offsets = bytes.intView(offsetsStart.toInt(), (buckets + 1).toInt())
            if (offsets[0] != 0 || offsets[buckets.toInt()] != postingsBytes) return null

            val groupStarts = dictionary.prefixGroupStarts(prefixLength)
            if (groupStarts.size - 1 != groups) return null

            return DictionarySpellIndex(
                dictionary = dictionary,
                bytes = bytes,
                offsets = offsets,
                postingsStart = postingsStart.toInt(),
                postingsBytes = postingsBytes,
                groupStarts = groupStarts,
                bucketShift = Long.SIZE_BITS - bucketBits,
                prefixLength = prefixLength,
                alphabet = CharArray(alphabetLength) { bytes.getChar(alphabetStart + it * Char.SIZE_BYTES) },
            )
        }

        /**
         * Начала групп слов с одинаковыми первыми [prefixLength] буквами — тем же правилом, что и при сборке:
         * новая группа там, где начало слова отличается от начала предыдущего. Последний элемент — размер словаря.
         */
        private fun WordDictionary.prefixGroupStarts(prefixLength: Int): IntArray {
            val starts = IntArray(size + 1)
            var count = 0
            for (id in 0 until size) {
                if (id == 0 || !hasSamePrefix(id - 1, id, prefixLength)) starts[count++] = id
            }
            starts[count] = size
            return starts.copyOf(count + 1)
        }

        private fun WordDictionary.hasSamePrefix(previous: Int, current: Int, prefixLength: Int): Boolean {
            val length = minOf(lengthAt(current), prefixLength)
            if (minOf(lengthAt(previous), prefixLength) != length) return false
            val text = text
            val previousStart = startAt(previous)
            val currentStart = startAt(current)
            for (offset in 0 until length) {
                if (text[previousStart + offset] != text[currentStart + offset]) return false
            }
            return true
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
