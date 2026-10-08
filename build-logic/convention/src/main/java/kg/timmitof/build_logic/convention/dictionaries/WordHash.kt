package kg.timmitof.build_logic.convention.dictionaries

/**
 * 64-битный хеш слова для бинарных ассетов (фильтр словоформ, индекс опечаток).
 *
 * Продублирован в рантайме (`keyboard/suggestion/data/.../WordHash.kt`): build-logic — отдельная
 * сборка, общего кода у них нет. Менять только вместе с версиями форматов [WordFormsFilter] и [SpellIndexWriter].
 */
internal object WordHash {

    private const val FNV_OFFSET = -0x340d631b7bdddcdbL
    private const val FNV_PRIME = 0x100000001b3L
    private const val MIX_FIRST = -0xae502812aa7333L
    private const val MIX_SECOND = -0x3b314601e57a13adL

    /**
     * FNV-1a по байтам UTF-16 (младший, потом старший) с финальным перемешиванием из MurmurHash3.
     * Не `String.hashCode()`: его 32 бит мало для миллионов слов, а реализация — не наш контракт.
     *
     * Хешируются первые [length] символов [word] без позиций [skipFirst] и [skipSecond] (`-1` — не пропускать):
     * так удаления для индекса опечаток считаются без создания строк. Результат зависит только
     * от оставшихся символов — «abc» без «b» и «ac» дают один хеш.
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
}
