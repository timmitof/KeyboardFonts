package kg.timmitof.keyboard.suggestion.data

/**
 * Копия `WordHash` из build-logic: FNV-1a по байтам UTF-16 + перемешивание MurmurHash3.
 * На нём держатся форматы `.forms` и `.spell` — менять только вместе с их версиями.
 */
internal object WordHash {

    private const val FNV_OFFSET = -0x340d631b7bdddcdbL
    private const val FNV_PRIME = 0x100000001b3L
    private const val MIX_FIRST = -0xae502812aa7333L
    private const val MIX_SECOND = -0x3b314601e57a13adL

    /**
     * Хеш первых [length] символов [word] без позиций [skipFirst] и [skipSecond] (`-1` — не пропускать):
     * удаления для индекса опечаток считаются без создания строк.
     */
    fun hash(
        word: CharSequence,
        length: Int = word.length,
        skipFirst: Int = -1,
        skipSecond: Int = -1,
    ): Long {
        var hash = FNV_OFFSET
        for (index in 0 until length) {
            if (index == skipFirst || index == skipSecond) continue
            val code = word[index].code
            hash = (hash xor (code and 0xFF).toLong()) * FNV_PRIME
            hash = (hash xor (code ushr 8).toLong()) * FNV_PRIME
        }
        hash = (hash xor (hash ushr 33)) * MIX_FIRST
        hash = (hash xor (hash ushr 33)) * MIX_SECOND
        return hash xor (hash ushr 33)
    }

    /**
     * Хеши начала слова (первые [prefixLength] букв) и всех его вариантов без не более чем [maxDeletes] букв —
     * ключи symmetric delete. Пустой вариант пропускается, как при сборке индекса. Повторы (двойные буквы)
     * не отсеиваются: вызывающий и так убирает одинаковых кандидатов.
     */
    inline fun forEachDelete(word: CharSequence, prefixLength: Int, maxDeletes: Int, action: (Long) -> Unit) {
        val length = minOf(word.length, prefixLength)
        if (length == 0) return

        action(hash(word, length))
        if (maxDeletes >= 1 && length > 1) {
            for (first in 0 until length) action(hash(word, length, first))
        }
        if (maxDeletes >= 2 && length > 2) {
            for (first in 0 until length) {
                for (second in first + 1 until length) action(hash(word, length, first, second))
            }
        }
    }
}
