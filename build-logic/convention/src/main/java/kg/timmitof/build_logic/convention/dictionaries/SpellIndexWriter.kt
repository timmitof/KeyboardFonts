package kg.timmitof.build_logic.convention.dictionaries

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File

/**
 * Индекс опечаток `<code>.spell` — symmetric delete, как в SymSpell, но собранный заранее и только
 * на одну правку: для начала слова (первые [PREFIX_LENGTH] букв) хранятся оно само и все варианты без одной буквы.
 * Вторую правку рантайм досчитывает на лету — перебирает исправления запроса (`DictionarySpellIndex`).
 *
 * Слова с одинаковым началом (`переключатель`, `переключателя` …) дают одни и те же ключи и в отсортированном
 * словаре идут подряд, поэтому индексируются не слова, а такие группы: номер группы — порядковый номер
 * её начала среди различных начал словаря. Рантайм восстанавливает группы из словаря тем же правилом.
 *
 * Ключ хранится не строкой, а корзиной по старшим `bucketBits` бит своего [WordHash] плюс отпечатком —
 * младшим байтом хеша. Отпечаток отсекает почти все чужие ключи той же корзины без проверки расстоянием:
 * при досчёте второй правки запрос заглядывает в тысячи корзин.
 *
 * Формат (big-endian, как пишет [DataOutputStream]), все поля — `Int`, если не сказано иное:
 * ```
 * MAGIC, VERSION, words, groups, prefixLength, indexedDeletes, bucketBits, entries, postingsBytes, alphabetLength
 * alphabet: alphabetLength × Char (UTF-16), плюс один нулевой Char, если длина нечётная (выравнивание на 4)
 * offsets: (2^bucketBits + 1) × Int — начало корзины в postings в байтах, последний элемент равен postingsBytes
 * postings: postingsBytes байт — записи корзин подряд; запись = varint(группа − предыдущая группа корзины), байт отпечатка
 * ```
 * Внутри корзины записи отсортированы по группе, затем по отпечатку; у первой записи разность считается от нуля.
 * Varint — беззнаковый LEB128: по 7 бит, старший бит — «дальше есть байт». Одна группа может встретиться
 * в корзине дважды (разность 0) — если два её ключа попали в одну корзину с разными отпечатками.
 * Алфавит нужен рантайму для перебора замен и вставок. Рантайм — `keyboard/suggestion/data/.../DictionarySpellIndex.kt`;
 * менять формат только вместе с [VERSION].
 *
 * @param words слова в порядке строк словаря `<code>.dict`.
 */
internal class SpellIndexWriter(
    private val words: List<String>,
    private val alphabet: String,
) {

    class Stats(val groups: Int, val entries: Int, val buckets: Int, val bytes: Long)

    fun writeTo(file: File): Stats {
        val groups = prefixGroups()

        val keyHashes = LongArray(MAX_KEYS_PER_GROUP)
        val distinctKeys = groups.sumOf { prefix -> prefix.collectKeys(keyHashes) }
        val bucketBits = bucketBitsFor(distinctKeys)
        val buckets = 1 shl bucketBits
        val shift = Long.SIZE_BITS - bucketBits

        // Запись упакована в Long так, что сортировка чисел = порядок файла: корзина, группа, отпечаток.
        val entries = LongArray(distinctKeys)
        var size = 0
        groups.forEachIndexed { group, prefix ->
            val count = prefix.collectKeys(keyHashes)
            for (index in 0 until count) {
                val hash = keyHashes[index]
                val bucket = hash ushr shift
                val fingerprint = hash and FINGERPRINT_MASK
                entries[size++] = (bucket shl BUCKET_SHIFT) or (group.toLong() shl GROUP_SHIFT) or fingerprint
            }
        }
        entries.sort()

        val offsets = IntArray(buckets + 1)
        val postings = ByteArrayOutputStream(size * 3)
        var written = 0
        var index = 0
        for (bucket in 0 until buckets) {
            offsets[bucket] = postings.size()
            var previous = 0
            while (index < size && (entries[index] ushr BUCKET_SHIFT).toInt() == bucket) {
                val entry = entries[index++]
                // Разные ключи группы с одинаковыми корзиной и отпечатком неразличимы — хватит одной записи.
                if (index > 1 && entry == entries[index - 2]) continue
                val group = ((entry ushr GROUP_SHIFT) and GROUP_MASK).toInt()
                postings.writeVarint(group - previous)
                postings.write((entry and FINGERPRINT_MASK).toInt())
                previous = group
                written++
            }
        }
        offsets[buckets] = postings.size()

        DataOutputStream(file.outputStream().buffered(BUFFER_SIZE)).use { out ->
            out.writeInt(MAGIC)
            out.writeInt(VERSION)
            out.writeInt(words.size)
            out.writeInt(groups.size)
            out.writeInt(PREFIX_LENGTH)
            out.writeInt(INDEXED_DELETES)
            out.writeInt(bucketBits)
            out.writeInt(written)
            out.writeInt(postings.size())
            out.writeInt(alphabet.length)
            alphabet.forEach { out.writeChar(it.code) }
            if (alphabet.length % 2 != 0) out.writeChar(0)
            offsets.forEach(out::writeInt)
            postings.writeTo(out)
        }

        return Stats(groups = groups.size, entries = written, buckets = buckets, bytes = file.length())
    }

    /** Различные начала слов в порядке словаря: одинаковые начала в отсортированном списке идут подряд. */
    private fun prefixGroups(): List<String> {
        val groups = ArrayList<String>(words.size)
        words.forEach { word ->
            val prefix = word.take(PREFIX_LENGTH)
            if (groups.lastOrNull() != prefix) groups += prefix
        }
        return groups
    }

    /**
     * Хеши ключей начала без повторов (двойные буквы дают одинаковые варианты): само начало и варианты
     * без одной буквы. Пустой вариант не индексируется — он совпал бы с любым коротким словом, как в SymSpell.
     */
    private fun String.collectKeys(into: LongArray): Int {
        var count = 0

        fun add(hash: Long) {
            for (index in 0 until count) if (into[index] == hash) return
            into[count++] = hash
        }

        add(WordHash.hash(this))
        if (length > 1) {
            for (skip in indices) add(WordHash.hash(this, length, skip))
        }
        return count
    }

    /** Корзин столько, чтобы в каждой было не больше [ENTRIES_PER_BUCKET] записей в среднем. */
    private fun bucketBitsFor(entries: Int): Int {
        var bits = MIN_BUCKET_BITS
        while (bits < MAX_BUCKET_BITS && (1L shl bits) * ENTRIES_PER_BUCKET < entries) bits++
        return bits
    }

    private fun ByteArrayOutputStream.writeVarint(value: Int) {
        var rest = value
        while (rest >= VARINT_CONTINUATION) {
            write((rest and VARINT_PAYLOAD) or VARINT_CONTINUATION)
            rest = rest ushr VARINT_BITS
        }
        write(rest)
    }

    companion object {
        /** `KFSI` — Keyboard Fonts Spell Index. */
        const val MAGIC = 0x4B465349
        const val VERSION = 2

        /** Как `prefixLength` в SymSpell: опечатки дальше седьмой буквы проверяются уже расстоянием. */
        const val PREFIX_LENGTH = 7

        /** Сколько букв удаляется из ключей слова. Рантайм рассчитан ровно на одну. */
        const val INDEXED_DELETES = 1

        /**
         * Меньше корзин — меньше таблица offsets и короче разности номеров, но больше записей читается
         * на каждый ключ запроса; чужие записи всё равно отсекает отпечаток.
         */
        private const val ENTRIES_PER_BUCKET = 16
        private const val MIN_BUCKET_BITS = 4

        /** Старший бит упакованной записи — знак: корзина занимает биты 40..62. */
        private const val MAX_BUCKET_BITS = 23

        private const val BUCKET_SHIFT = 40
        private const val GROUP_SHIFT = 8
        private const val GROUP_MASK = 0xFFFFFFFFL
        private const val FINGERPRINT_MASK = 0xFFL

        /** Само начало и 7 вариантов без одной буквы. */
        private const val MAX_KEYS_PER_GROUP = 1 + PREFIX_LENGTH

        private const val VARINT_BITS = 7
        private const val VARINT_PAYLOAD = 0x7F
        private const val VARINT_CONTINUATION = 0x80

        private const val BUFFER_SIZE = 1 shl 16
    }
}
