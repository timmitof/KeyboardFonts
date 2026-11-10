package kg.timmitof.keyboard.suggestion.data

/** Общие для тестов словари и модели. */

internal fun correctorOf(dictionary: WordDictionary): SpellCorrector {
    val index = PersonalSpellIndex(dictionary::scoreOf)
    for (i in 0 until dictionary.size) index.add(dictionary.wordAt(i))
    return SpellCorrector(
        dictionaryIndex = index,
        personal = PersonalSpellIndex(dictionary::scoreOf),
        isDictionaryWord = dictionary::contains,
    )
}

internal fun dictionaryOf(vararg entries: Pair<String, Int>): WordDictionary =
    WordDictionary.parse(
        entries
            .sortedBy(Pair<String, Int>::first)
            .joinToString(separator = "\n") { (word, score) -> "$word\t$score" },
    )

internal fun modelOf(vararg entries: Pair<String, Int>): LanguageModel {
    val dictionary = dictionaryOf(*entries)
    return LanguageModel(
        dictionary = dictionary,
        bigrams = BigramTable.Empty,
        spellCorrector = correctorOf(dictionary),
    )
}
