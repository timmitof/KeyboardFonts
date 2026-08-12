package kg.timmitof.keyboard.data.suggestion

/**
 * Языковая модель раскладки: словарь слов + статистика пар.
 */
internal class LanguageModel(
    val dictionary: WordDictionary,
    val bigrams: BigramTable,
) {
    companion object {
        val Empty = LanguageModel(WordDictionary.Empty, BigramTable.Empty)
    }
}
