package kg.timmitof.keyboard.suggestion.data

import com.darkrockstudios.symspellkt.common.SpellCheckSettings
import com.darkrockstudios.symspellkt.common.Verbosity
import com.darkrockstudios.symspellkt.impl.SymSpell

/** SymSpell (symmetric delete): индекс удалений строится заранее, без линейного прохода по словарю. */
internal class SpellCorrector private constructor(
    private val symSpell: SymSpell,
    private val dictionary: WordDictionary,
) {

    class Correction(val word: String, val distance: Int, val score: Int)

    @Synchronized
    fun corrections(query: String, maxDistance: Double = MAX_EDIT_DISTANCE): List<Correction> =
        symSpell.lookup(query, Verbosity.All, maxDistance)
            .map { item ->
                Correction(
                    word = item.term,
                    distance = item.distance.toInt(),
                    score = dictionary.scoreOf(item.term),
                )
            }

    @Synchronized
    fun addWord(word: String, frequency: Int) {
        symSpell.createDictionaryEntry(word, frequency)
    }

    companion object {

        const val MAX_EDIT_DISTANCE = 2.0

        private const val PREFIX_LENGTH = 7
        private const val TOP_K = 12

        fun build(dictionary: WordDictionary): SpellCorrector {
            val settings = SpellCheckSettings(
                maxEditDistance = MAX_EDIT_DISTANCE,
                prefixLength = PREFIX_LENGTH,
                countThreshold = 1,
                topK = TOP_K,
            )
            val symSpell = SymSpell(settings)

            for (i in 0 until dictionary.size) {
                symSpell.createDictionaryEntry(dictionary.wordAt(i), dictionary.scoreAt(i))
            }

            return SpellCorrector(symSpell, dictionary)
        }

        val Empty = SpellCorrector(
            symSpell = SymSpell(
                SpellCheckSettings(maxEditDistance = MAX_EDIT_DISTANCE),
            ),
            dictionary = WordDictionary.Empty,
        )
    }
}
