package kg.timmitof.keyboard.data.emoji

import kg.timmitof.keyboard.data.AssetTextLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Поиск эмодзи по ключевым словам из ассета `emoji/annotations.tsv`.
 */
@Singleton
class EmojiSearchIndex @Inject constructor(
    private val assetTextLoader: AssetTextLoader,
) {

    @Volatile
    private var entries: List<Entry>? = null

    suspend fun search(query: String): List<String> = withContext(Dispatchers.Default) {
        val words = query.toQueryWords()
        if (words.isEmpty()) return@withContext emptyList()

        index().mapNotNull { entry -> entry.score(words)?.let { entry to it } }
            .sortedByDescending { (_, score) -> score }
            .take(MAX_RESULTS)
            .map { (entry, _) -> entry.emoji }
    }

    /** Готовит индекс заранее, чтобы первый запрос не ждал разбора ассета. */
    suspend fun prefetch() {
        withContext(Dispatchers.Default) { index() }
    }

    private suspend fun index(): List<Entry> = entries ?: load().also { entries = it }

    /**
     * Читает словарь и оставляет только эмодзи, которые устройство умеет рисовать.
     */
    private suspend fun load(): List<Entry> {
        val supported = EmojiCatalog.getCategories().flatMapTo(mutableSetOf()) { it.emojis }
        val text = assetTextLoader.loadText(ANNOTATIONS_PATH).orEmpty()

        return text.lineSequence().mapNotNull { line ->
            val columns = line.split(COLUMN)
            if (columns.size < 3) return@mapNotNull null

            val emoji = columns[0]
            if (emoji !in supported) return@mapNotNull null

            val russian = columns[1].normalize().split(KEYWORD).filter { it.isNotEmpty() }
            val english = columns[2].normalize().split(KEYWORD).filter { it.isNotEmpty() }

            // Названия обоих языков — вперёд: по ним совпадение весит больше.
            val names = listOfNotNull(russian.firstOrNull(), english.firstOrNull())
            if (names.isEmpty()) return@mapNotNull null

            val keywords = (names + russian.drop(1) + english.drop(1)).joinToString(KEYWORD_TEXT)

            Entry(
                emoji = emoji,
                keywords = keywords,
                nameEnd = names.joinToString(KEYWORD_TEXT).length,
                isPopular = emoji in EmojiPopularity.frequent
            )
        }.toList()
    }

    /**
     * Совпадение эмодзи с запросом или `null`, если хотя бы одно слово не найдено.
     */
    private fun Entry.score(words: List<String>): Int? {
        val total = words.fold(0) { sum, word ->
            val best = matchScore(word)
            if (best == 0) return null
            sum + best
        }
        return if (isPopular) total + POPULAR_BONUS else total
    }

    /**
     * Лучшее совпадение слова запроса с ключевыми словами эмодзи.
     */
    private fun Entry.matchScore(word: String): Int {
        var best = 0
        var index = keywords.indexOf(word)

        while (index >= 0) {
            val end = index + word.length
            val startsKeyword = index == 0 || keywords[index - 1] == KEYWORD
            val endsKeyword = end == keywords.length || keywords[end] == KEYWORD

            val score = when {
                !startsKeyword && !keywords[index - 1].isWordBreak() -> 0
                startsKeyword && endsKeyword -> EXACT_KEYWORD
                endsKeyword || keywords[end].isWordBreak() -> EXACT_WORD
                else -> PREFIX_WORD
            }

            if (score > 0) {
                val weighted = if (index < nameEnd) score + NAME_BONUS else score
                if (weighted > best) best = weighted
            }

            index = keywords.indexOf(word, index + 1)
        }
        return best
    }

    private fun Char.isWordBreak(): Boolean = this in WORD_SEPARATORS

    private fun String.normalize(): String = lowercase().replace('ё', 'е')

    /**
     * Запрос: слова в нижнем регистре, с заменой народных названий на словарные.
     */
    private fun String.toQueryWords(): List<String> = normalize()
        .split(*WORD_SEPARATORS)
        .filter { it.length >= MIN_WORD_LENGTH }
        .map { EmojiPopularity.aliases[it] ?: it }

    /**
     * @param keywords все ключевые слова через [KEYWORD]; названия идут первыми.
     * @param nameEnd граница названий внутри [keywords].
     */
    private data class Entry(
        val emoji: String,
        val keywords: String,
        val nameEnd: Int,
        val isPopular: Boolean,
    )

    private companion object {
        const val ANNOTATIONS_PATH = "emoji/annotations.tsv"
        const val MAX_RESULTS = 60

        const val COLUMN = '\t'
        const val KEYWORD = '|'
        const val KEYWORD_TEXT = "|"
        val WORD_SEPARATORS = charArrayOf(' ', ',', ':', '-', '_')

        const val MIN_WORD_LENGTH = 2

        const val EXACT_KEYWORD = 8
        const val EXACT_WORD = 6
        const val PREFIX_WORD = 4
        const val NAME_BONUS = 3
        const val POPULAR_BONUS = 4
    }
}
