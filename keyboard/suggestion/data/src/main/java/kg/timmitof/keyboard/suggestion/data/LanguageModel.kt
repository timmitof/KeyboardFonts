package kg.timmitof.keyboard.suggestion.data

internal class LanguageModel(
    val dictionary: WordDictionary,
    val bigrams: BigramTable,
    val spellCorrector: SpellCorrector,
) {
    companion object {
        val Empty = LanguageModel(WordDictionary.Empty, BigramTable.Empty, SpellCorrector.Empty)
    }
}
