package kg.timmitof.keyboard.suggestion.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Словарь и биграммы доступны сразу, индекс опечаток [spellIndex] строится в фоне
 * и дольше всего: пока он не готов, подсказки работают без исправлений.
 */
internal class LanguageModel(
    val dictionary: WordDictionary,
    val bigrams: BigramTable,
    val spellIndex: Deferred<SpellCorrector>,
) {

    /** Готовый индекс или `null`, если он ещё строится (или сборка упала). */
    @OptIn(ExperimentalCoroutinesApi::class)
    val spellCorrector: SpellCorrector?
        get() = if (spellIndex.isCompleted && !spellIndex.isCancelled) spellIndex.getCompleted() else null

    companion object {
        val Empty = LanguageModel(
            WordDictionary.Empty,
            BigramTable.Empty,
            CompletableDeferred(SpellCorrector.Empty),
        )
    }
}
