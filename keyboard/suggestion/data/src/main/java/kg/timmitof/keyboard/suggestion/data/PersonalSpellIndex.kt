package kg.timmitof.keyboard.suggestion.data

import java.util.concurrent.ConcurrentHashMap

/**
 * Индекс опечаток для выученных слов: в файл словаря они не попадают, а слов немного (сотни),
 * поэтому тот же symmetric delete, что у [DictionarySpellIndex], держится в памяти — хеш варианта → слова.
 *
 * Поиск идёт на каждое нажатие, обучение — в фоне: структуры конкурентные, массивы слов заменяются целиком.
 *
 * @param scoreOf частота слова в словаре; у слов пользователя обычно 0.
 */
internal class PersonalSpellIndex(private val scoreOf: (String) -> Int) : SpellIndex {

    private val words = ConcurrentHashMap.newKeySet<String>()

    private val deletes = ConcurrentHashMap<Long, Array<String>>()

    fun add(word: String) {
        if (word.isEmpty() || !words.add(word)) return
        WordHash.forEachDelete(word, SpellIndex.PREFIX_LENGTH, SpellIndex.MAX_DISTANCE) { hash ->
            deletes.merge(hash, arrayOf(word)) { current, _ -> if (word in current) current else current + word }
        }
    }

    fun remove(word: String) {
        if (!words.remove(word)) return
        WordHash.forEachDelete(word, SpellIndex.PREFIX_LENGTH, SpellIndex.MAX_DISTANCE) { hash ->
            deletes.computeIfPresent(hash) { _, current ->
                current.filter { it != word }.takeIf { it.isNotEmpty() }?.toTypedArray()
            }
        }
    }

    override fun corrections(query: String, maxDistance: Int): List<SpellIndex.Correction> {
        if (query.isEmpty() || words.isEmpty()) return emptyList()
        val distanceLimit = minOf(maxDistance, SpellIndex.MAX_DISTANCE)

        val candidates = HashSet<String>()
        WordHash.forEachDelete(query, SpellIndex.PREFIX_LENGTH, distanceLimit) { hash ->
            deletes[hash]?.let { candidates.addAll(it) }
        }

        val distance = EditDistance()
        return candidates
            .mapNotNull { word ->
                // Слово могли забыть, пока шёл поиск: в корзинах оно исчезает не одновременно.
                if (word !in words) return@mapNotNull null
                val found = distance.between(query, word, 0, word.length, distanceLimit)
                if (found <= distanceLimit) SpellIndex.Correction(word, found, scoreOf(word)) else null
            }
            .sortedWith(SpellIndex.ORDER)
            .take(SpellIndex.TOP_K)
    }

    /** Слов мало — достаточно пройти по всем. */
    override fun prefixCorrections(query: String, limit: Int, lengthPenalty: Int): List<SpellIndex.Correction> {
        if (query.isEmpty() || limit <= 0 || words.isEmpty()) return emptyList()

        val distance = EditDistance()
        return words
            .filter { word -> !word.startsWith(query) && distance.isPrefixWithinOneEdit(query, word) }
            .map { word -> SpellIndex.Correction(word, PREFIX_DISTANCE, scoreOf(word)) }
            .sortedByDescending { SpellIndex.prefixRank(it.score, it.word.length, query, lengthPenalty) }
            .take(limit)
    }

    private companion object {
        const val PREFIX_DISTANCE = 1
    }
}
