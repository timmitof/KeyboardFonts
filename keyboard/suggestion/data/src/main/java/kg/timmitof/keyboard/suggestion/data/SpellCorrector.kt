package kg.timmitof.keyboard.suggestion.data

/**
 * Исправление опечаток: готовый индекс словаря ([DictionarySpellIndex], собирается при сборке)
 * плюс маленький индекс выученных слов в памяти ([PersonalSpellIndex]). Оба только читаются на нажатие,
 * поэтому корректор готов сразу после загрузки модели и не требует блокировок.
 *
 * @param dictionaryIndex индекс словаря; если файла нет — [SpellIndex] без результатов.
 * @param isDictionaryWord есть ли слово в словаре: такие уже в [dictionaryIndex], в личный индекс не попадают.
 */
internal class SpellCorrector(
    private val dictionaryIndex: SpellIndex,
    private val personal: PersonalSpellIndex,
    private val isDictionaryWord: (String) -> Boolean,
) {

    fun corrections(query: String, maxDistance: Int = MAX_EDIT_DISTANCE): List<SpellIndex.Correction> =
        merge(
            dictionaryIndex.corrections(query, maxDistance),
            personal.corrections(query, maxDistance),
        )

    /** Слова, начало которых в одной правке от [query]; подробнее — [SpellIndex.prefixCorrections]. */
    fun prefixCorrections(query: String, limit: Int, lengthPenalty: Int): List<SpellIndex.Correction> =
        merge(
            dictionaryIndex.prefixCorrections(query, limit, lengthPenalty),
            personal.prefixCorrections(query, limit, lengthPenalty),
        )

    fun addWord(word: String) {
        if (!isDictionaryWord(word)) personal.add(word)
    }

    /** Выученное слово забыто (отменённая автозамена) — больше его не предлагаем как исправление. */
    fun removeWord(word: String) {
        personal.remove(word)
    }

    /** Личный индекс обычно пуст или мал — без лишних копий в частом случае. */
    private fun merge(
        dictionary: List<SpellIndex.Correction>,
        personal: List<SpellIndex.Correction>,
    ): List<SpellIndex.Correction> = when {
        personal.isEmpty() -> dictionary
        dictionary.isEmpty() -> personal
        else -> {
            val known = dictionary.mapTo(HashSet(dictionary.size)) { it.word }
            dictionary + personal.filter { it.word !in known }
        }
    }

    companion object {

        const val MAX_EDIT_DISTANCE = SpellIndex.MAX_DISTANCE

        fun create(dictionary: WordDictionary, dictionaryIndex: SpellIndex?): SpellCorrector =
            SpellCorrector(
                dictionaryIndex = dictionaryIndex ?: NoSpellIndex,
                personal = PersonalSpellIndex(dictionary::scoreOf),
                isDictionaryWord = dictionary::contains,
            )

        fun empty(): SpellCorrector = create(WordDictionary.Empty, dictionaryIndex = null)
    }

    /** Языка без файла индекса: исправления только по выученным словам. */
    private object NoSpellIndex : SpellIndex {
        override fun corrections(query: String, maxDistance: Int) = emptyList<SpellIndex.Correction>()

        override fun prefixCorrections(query: String, limit: Int, lengthPenalty: Int) =
            emptyList<SpellIndex.Correction>()
    }
}
