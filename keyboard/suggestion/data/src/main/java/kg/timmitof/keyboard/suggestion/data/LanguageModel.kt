package kg.timmitof.keyboard.suggestion.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Словарь и биграммы доступны сразу, индекс опечаток [spellIndex] строится в фоне
 * и дольше всего: пока он не готов, подсказки работают без исправлений.
 *
 * @param forms все словоформы языка, а не только частые из [dictionary]; у языка без списка форм — пустой.
 */
internal class LanguageModel(
    val dictionary: WordDictionary,
    val bigrams: BigramTable,
    val spellIndex: Deferred<SpellCorrector>,
    private val forms: WordForms = WordForms.Empty,
) {

    /** Готовый индекс или `null`, если он ещё строится (или сборка упала). */
    @OptIn(ExperimentalCoroutinesApi::class)
    val spellCorrector: SpellCorrector?
        get() = if (spellIndex.isCompleted && !spellIndex.isCancelled) spellIndex.getCompleted() else null

    /**
     * Настоящая словоформа языка, пусть и редкая («переключателя», «толп»): автозамена её не трогает.
     * Ошибается в «да» примерно в 1 % случаев — тогда опечатка просто останется как есть.
     */
    fun isWordForm(word: String): Boolean = forms.mightContain(word)

    /**
     * Нет в словаре и среди словоформ, но в одной правке есть частое словарное слово — скорее всего, промах по клавише.
     * Зовётся при завершении слова, а не на нажатие: поиск по индексу на каждую букву не нужен.
     */
    fun isLikelyTypo(word: String, corrector: SpellCorrector): Boolean =
        !dictionary.contains(word) && !isWordForm(word) &&
                corrector.corrections(word, maxDistance = 1.0).any { correction ->
                    correction.distance == 1 && correction.score >= TYPO_NEIGHBOR_MIN_SCORE
                }

    companion object {
        val Empty = LanguageModel(
            WordDictionary.Empty,
            BigramTable.Empty,
            CompletableDeferred(SpellCorrector.Empty),
        )

        /** Шкала 1..1000; от 600 и выше — примерно 1,2–1,6 тыс. самых частых слов, где опечатки и случаются. */
        const val TYPO_NEIGHBOR_MIN_SCORE = 600
    }
}
