package kg.timmitof.keyboard.suggestion.data

/**
 * Поиск слов рядом с набранным. Две реализации: [DictionarySpellIndex] — готовый файл словаря,
 * [PersonalSpellIndex] — выученные слова пользователя в памяти; вместе их сводит [SpellCorrector].
 */
internal interface SpellIndex {

    /** @param score частота словаря (1..1000); у слова не из словаря — 0. */
    class Correction(val word: String, val distance: Int, val score: Int)

    /**
     * Слова целиком в пределах [maxDistance] правок (Дамерау, с перестановкой соседних букв), включая
     * само [query] с расстоянием 0, если оно известно. Не больше [TOP_K]: ближние, среди них — частые.
     */
    fun corrections(query: String, maxDistance: Int): List<Correction>

    /**
     * Слова, начало которых отличается от [query] одной правкой, без слов, начинающихся точно на [query]
     * (это обычное дополнение). Расстояние у всех — 1. Отбираются [limit] лучших по частоте
     * за вычетом [lengthPenalty] за каждую букву сверх набранных.
     */
    fun prefixCorrections(query: String, limit: Int, lengthPenalty: Int): List<Correction>

    companion object {

        /** Как `prefixLength` в SymSpell: варианты с удалениями строятся из первых семи букв. */
        const val PREFIX_LENGTH = 7

        const val MAX_DISTANCE = 2

        /** Столько возвращал SymSpell (`topK`): дальше — редкие слова, которые всё равно не покажутся. */
        const val TOP_K = 12

        /** Порядок SymSpell: сначала ближние, среди равных — частые. */
        val ORDER: Comparator<Correction> =
            compareBy<Correction> { it.distance }.thenByDescending { it.score }.thenBy { it.word }

        /** Отбор лучших префиксных исправлений: тот же счёт, что и у точного дополнения в движке. */
        fun prefixRank(score: Int, wordLength: Int, query: String, lengthPenalty: Int): Int =
            score - lengthPenalty * (wordLength - query.length).coerceAtLeast(0)
    }
}
