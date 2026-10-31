package kg.timmitof.keyboard.suggestion.data

/**
 * Всё, что нужно подсказкам одного языка. Индекс опечаток собран при сборке и только отображается
 * в память, поэтому [spellCorrector] готов вместе со словарём — без фонового построения.
 *
 * @param forms все словоформы языка, а не только частые из [dictionary]; у языка без списка форм — пустой.
 */
internal class LanguageModel(
    val dictionary: WordDictionary,
    val bigrams: BigramTable,
    val spellCorrector: SpellCorrector,
    private val forms: WordForms = WordForms.Empty,
) {

    /**
     * Настоящая словоформа языка, пусть и редкая («переключателя», «толп»): автозамена её не трогает.
     * Ошибается в «да» примерно в 3 % случаев (`formsFalsePositiveRate`) — тогда опечатка просто останется как есть.
     */
    fun isWordForm(word: String): Boolean = forms.mightContain(word)

    /**
     * Нет в словаре и среди словоформ, но в одной правке есть частое словарное слово — скорее всего, промах по клавише.
     * Зовётся при завершении слова, а не на нажатие: поиск по индексу на каждую букву не нужен.
     */
    fun isLikelyTypo(word: String): Boolean =
        !dictionary.contains(word) && !isWordForm(word) &&
                spellCorrector.corrections(word, maxDistance = 1).any { correction ->
                    correction.distance == 1 && correction.score >= TYPO_NEIGHBOR_MIN_SCORE
                }

    companion object {

        /** Свой экземпляр на язык: личный индекс опечаток изменяемый, делить его между языками нельзя. */
        fun empty(): LanguageModel = LanguageModel(
            WordDictionary.Empty,
            BigramTable.Empty,
            SpellCorrector.empty(),
        )

        /** Шкала 1..1000; от 600 и выше — примерно 1,2–1,6 тыс. самых частых слов, где опечатки и случаются. */
        const val TYPO_NEIGHBOR_MIN_SCORE = 600
    }
}
