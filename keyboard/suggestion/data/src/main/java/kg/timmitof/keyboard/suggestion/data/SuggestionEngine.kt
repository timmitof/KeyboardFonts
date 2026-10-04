package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.keyboard.data.font.FontDecoder
import kg.timmitof.keyboard.suggestion.domain.model.SuggestionRequest
import kg.timmitof.keyboard.suggestion.domain.model.WordSuggestion
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ln

/**
 * Сводит подсказки одной шкалой очков из источников: частота словаря, пары слов
 * (словарные и выученные), личная статистика, слова текущего поля. Опечатки — через [SpellCorrector].
 */
@Singleton
class SuggestionEngine @Inject constructor() {

    internal suspend fun suggest(
        request: SuggestionRequest,
        model: LanguageModel,
        user: UserLanguageModel,
    ): List<WordSuggestion> {
        val context = request.context
        val typed = FontDecoder.decode(context.composingWord)
        val previous = context.previousWord.toDictionaryForm()
        val surrounding = context.surroundingWords
            .take(SURROUNDING_LIMIT)
            .mapTo(HashSet(SURROUNDING_LIMIT)) { it.toDictionaryForm() }

        val boosts = Boosts(model, user, previous, surrounding)

        return if (typed.isEmpty()) {
            predict(request, boosts, model, user, previous)
        } else {
            complete(request, boosts, model, user, previous, typed)
        }
    }

    /** Без контекста список пуст: словарный топ бессмысленен и прятал бы карусель шрифтов. */
    private fun predict(
        request: SuggestionRequest,
        boosts: Boosts,
        model: LanguageModel,
        user: UserLanguageModel,
        previous: String,
    ): List<WordSuggestion> {
        val candidates = HashMap<String, Int>(32)

        // Сила связи с предыдущим словом уже в надбавках — здесь только список.
        fun offer(word: String, score: Int = 0) {
            if (word.length < MIN_WORD_LENGTH) return
            candidates[word] = maxOf(candidates[word] ?: 0, score + boosts.of(word))
        }

        if (previous.isNotEmpty()) {
            user.followersOf(previous).keys.forEach { word -> offer(word) }
            model.bigrams.after(previous, PREDICTION_FOLLOWERS).forEach { follower ->
                offer(follower.word)
            }
        } else {
            user.frequentWords(START_WORDS).forEach { word -> offer(word, START_WORD_SCORE) }
        }

        val capitalize = request.isShifted || request.context.isSentenceStart

        return candidates.entries
            .sortedByDescending { it.value }
            .take(MAX_SUGGESTIONS)
            .map { WordSuggestion(text = it.key.capitalizedIf(capitalize)) }
    }

    private fun complete(
        request: SuggestionRequest,
        boosts: Boosts,
        model: LanguageModel,
        user: UserLanguageModel,
        previous: String,
        typed: String,
    ): List<WordSuggestion> {
        val query = typed.foldToDictionary()
        val dictionary = model.dictionary

        val candidates = HashMap<String, Candidate>(32)

        fun offer(word: String, score: Int, distance: Int) {
            if (word == query) return
            val total = score + boosts.of(word) - completionPenalty(word, query)
            val current = candidates[word]
            if (current == null || total > current.score) {
                candidates[word] = Candidate(word, total, distance)
            }
        }

        val isKnown = dictionary.contains(query) || user.knows(query)

        dictionary.rankByPrefix(query).forEach { index ->
            offer(dictionary.wordAt(index), dictionary.scoreAt(index), distance = 0)
        }

        if (!isKnown) {
            maxTypoDistance(query)?.let { maxDistance ->
                model.spellCorrector.corrections(query, maxDistance).forEach { correction ->
                    offer(
                        word = correction.word,
                        score = correction.score - typoPenalty(correction.distance) + proximityBonus(query, correction.word),
                        distance = correction.distance,
                    )
                }
            }
        }

        user.wordsWithPrefix(query).forEach { word -> offer(word, USER_WORD_SCORE, distance = 0) }
        boosts.surroundingWithPrefix(query).forEach { word -> offer(word, USER_WORD_SCORE, distance = 0) }

        val expected = expectedAfter(previous, query, model, user, ::offer)

        val ranked = candidates.values.sortedByDescending { it.score }
        val best = ranked.firstOrNull() ?: return emptyList()
        val isAutoCorrect = request.allowsAutoCorrect && !isKnown &&
                best.replaces(query, expected) &&
                typed.isCorrectable(request.context.isSentenceStart)

        return buildList(MAX_SUGGESTIONS) {
            add(WordSuggestion(text = typed, isLiteral = !isKnown))

            ranked.take(MAX_SUGGESTIONS - 1).forEachIndexed { index, candidate ->
                add(
                    WordSuggestion(
                        text = candidate.word.matchCaseOf(typed),
                        isAutoCorrect = index == 0 && isAutoCorrect,
                    )
                )
            }
        }
    }

    /**
     * Слова на [query], которых ждёт фраза (из словарных и выученных пар), отдаются в общий отбор.
     * Возвращает их множество — по нему видно, что дополнение подсказано контекстом.
     */
    private fun expectedAfter(
        previous: String,
        query: String,
        model: LanguageModel,
        user: UserLanguageModel,
        offer: (word: String, score: Int, distance: Int) -> Unit,
    ): Set<String> {
        if (previous.isEmpty()) return emptySet()

        val expected = HashSet<String>(EXPECTED_CAPACITY)

        model.bigrams.followersWithPrefix(previous, query).forEach { follower ->
            expected += follower.word
            offer(follower.word, model.dictionary.scoreOf(follower.word), 0)
        }
        user.followersOf(previous).keys.forEach { word ->
            if (word.length <= query.length || !word.startsWith(query)) return@forEach
            expected += word
            offer(word, maxOf(model.dictionary.scoreOf(word), USER_WORD_SCORE), 0)
        }

        return expected
    }

    /**
     * Можно ли молча подставить кандидата при пробеле. Дополнение — если набрано достаточно
     * («прив» → «привет») или оно следует из фразы («как д» → «дела»).
     */
    private fun Candidate.replaces(query: String, expected: Set<String>): Boolean = when {
        distance > 0 -> distance <= MAX_AUTO_CORRECT_DISTANCE &&
                word.length <= query.length + AUTO_CORRECT_EXTRA_CHARS

        else -> word in expected || query.length >= AUTO_COMPLETE_MIN_CHARS
    }

    /** Слово с большой буквы посреди предложения — имя или сокращение, не исправляем. */
    private fun String.isCorrectable(isSentenceStart: Boolean): Boolean =
        isSentenceStart || firstOrNull()?.isUpperCase() != true

    private fun completionPenalty(word: String, query: String): Int =
        COMPLETION_PENALTY * (word.length - query.length).coerceAtLeast(0)

    /** Постоянная часть важнее переменной: слово с точным началом почти всегда лучше «похожего». */
    private fun typoPenalty(distance: Int): Int = TYPO_BASE_PENALTY + TYPO_PENALTY * distance

    /** Для коротких обрывков исправление вредно: точное дополнение уже есть в префиксном поиске. */
    private fun maxTypoDistance(query: String): Double? = when {
        query.length < MIN_TYPO_LENGTH -> null
        query.length < TWO_EDITS_MIN_LENGTH -> 1.0
        else -> SpellCorrector.MAX_EDIT_DISTANCE
    }

    private fun proximityBonus(query: String, word: String): Int {
        if (query.length != word.length) return 0

        var mismatch = -1
        query.indices.forEach { index ->
            if (query[index] != word[index]) {
                if (mismatch >= 0) return 0
                mismatch = index
            }
        }
        return mismatch.takeIf { it >= 0 && KeyProximity.areAdjacent(query[it], word[it]) }
            ?.let { ADJACENT_KEY_BONUS }
            ?: 0
    }

    private fun WordDictionary.rankByPrefix(query: String): IntArray {
        val ranking = TopIndices(RANKING_SIZE)
        prefixRange(query).forEach { index ->
            ranking.offer(index, scoreAt(index) - COMPLETION_PENALTY * (lengthAt(index) - query.length))
        }
        return ranking.indices()
    }

    /** Надбавки, не зависящие от способа поиска слова: личная частота, пары, лексика поля. */
    private class Boosts(
        private val model: LanguageModel,
        private val user: UserLanguageModel,
        private val previous: String,
        private val surrounding: Set<String>,
    ) {

        fun of(word: String): Int {
            var bonus = personalScore(user.countOf(word))

            if (previous.isNotEmpty()) {
                bonus += learnedPairScore(user.pairCount(previous, word))
                bonus += model.bigrams.scoreOf(previous, word) * DICTIONARY_PAIR_WEIGHT / WEIGHT_UNIT
            }
            if (word in surrounding) bonus += SURROUNDING_BONUS
            if (user.isRecent(word)) bonus += RECENT_BONUS

            return bonus
        }

        fun surroundingWithPrefix(query: String): List<String> =
            surrounding.filter { it.length > query.length && it.startsWith(query) }
    }

    private class Candidate(val word: String, val score: Int, val distance: Int)

    private companion object {

        const val MAX_SUGGESTIONS = 3

        const val RANKING_SIZE = 12

        const val MIN_WORD_LENGTH = 2

        const val MIN_TYPO_LENGTH = 3

        const val TWO_EDITS_MIN_LENGTH = 8

        const val MAX_AUTO_CORRECT_DISTANCE = 1

        const val SURROUNDING_LIMIT = 32

        // Веса. Базовая шкала — частота словаря, 1..1000.
        const val COMPLETION_PENALTY = 24
        const val TYPO_PENALTY = 120
        const val TYPO_BASE_PENALTY = 150
        const val ADJACENT_KEY_BONUS = TYPO_PENALTY
        const val SURROUNDING_BONUS = 220
        const val RECENT_BONUS = 140
        const val USER_WORD_SCORE = 500
        const val START_WORD_SCORE = 200
        const val START_WORDS = 3

        /** Больше единицы намеренно: связь с предыдущим словом — самый сильный сигнал. */
        const val DICTIONARY_PAIR_WEIGHT = 15
        const val WEIGHT_UNIT = 10

        const val PERSONAL_WEIGHT = 110
        const val PERSONAL_LIMIT = 450

        // Своя пара слов обгоняет словарную уже после нескольких повторов.
        const val PAIR_WEIGHT = 700
        const val PAIR_LIMIT = 2000

        const val AUTO_CORRECT_EXTRA_CHARS = 1

        const val AUTO_COMPLETE_MIN_CHARS = 3

        const val PREDICTION_FOLLOWERS = 24

        const val EXPECTED_CAPACITY = 8

        fun personalScore(count: Int): Int =
            if (count <= 0) 0 else minOf(PERSONAL_LIMIT, (PERSONAL_WEIGHT * ln(1.0 + count)).toInt())

        fun learnedPairScore(count: Int): Int =
            if (count <= 0) 0 else minOf(PAIR_LIMIT, (PAIR_WEIGHT * ln(1.0 + count)).toInt())

        fun String.capitalizedIf(capitalize: Boolean): String =
            if (capitalize) replaceFirstChar(Char::uppercaseChar) else this

        /** Повторяет регистр набранного: `При` → `Привет`, `ПРИ` → `ПРИВЕТ`. */
        fun String.matchCaseOf(typed: String): String = when {
            typed.length > 1 && typed.all { !it.isLetter() || it.isUpperCase() } -> uppercase()
            typed.firstOrNull()?.isUpperCase() == true -> capitalizedIf(true)
            else -> this
        }
    }
}
