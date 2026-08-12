package kg.timmitof.keyboard.data.suggestion

import kg.timmitof.keyboard.data.font.FontDecoder
import kg.timmitof.keyboard.domain.model.SuggestionRequest
import kg.timmitof.keyboard.domain.model.WordSuggestion
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ln

/**
 * Собирает подсказки из четырёх источников и сводит их одной шкалой очков:
 *
 * - словарь языка — базовая частота слова;
 * - пары слов (словарные и выученные) — что обычно идёт после предыдущего слова;
 * - личная статистика пользователя — что он пишет чаще всего;
 * - текст самого поля — имена и слова текущего разговора.
 *
 * Пока слово набирается, к этому добавляется цена опечаток: словарь просматривается
 * не только по точному префиксу, но и по «почти совпадающим» словам ([PrefixMatcher]).
 */
@Singleton
class SuggestionEngine @Inject constructor() {

    internal fun suggest(
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
            complete(request, boosts, model, user, typed)
        }
    }

    // region Предсказание следующего слова
    /**
     * Слово ещё не начато: подсказываем продолжение фразы.
     *
     * Без контекста показывать словарный топ бессмысленно — панель подсказок
     * тогда просто прячет карусель шрифтов, поэтому список остаётся пустым.
     */
    private fun predict(
        request: SuggestionRequest,
        boosts: Boosts,
        model: LanguageModel,
        user: UserLanguageModel,
        previous: String,
    ): List<WordSuggestion> {
        val candidates = HashMap<String, Int>(32)

        // Сила связи с предыдущим словом уже учтена в надбавках — здесь только сам список.
        fun offer(word: String, score: Int = 0) {
            if (word.length < MIN_WORD_LENGTH) return
            candidates[word] = maxOf(candidates[word] ?: 0, score + boosts.of(word))
        }

        if (previous.isNotEmpty()) {
            user.followersOf(previous).keys.forEach { word -> offer(word) }
            model.bigrams.after(previous).forEach { follower -> offer(follower.word) }
        } else {
            // Начало сообщения: подсказываем то, с чего пользователь обычно начинает.
            user.frequentWords(START_WORDS).forEach { word -> offer(word, START_WORD_SCORE) }
        }

        val capitalize = request.isShifted || request.context.isSentenceStart

        return candidates.entries
            .sortedByDescending { it.value }
            .take(MAX_SUGGESTIONS)
            .map { WordSuggestion(text = it.key.capitalizedIf(capitalize)) }
    }
    // endregion

    // region Дополнение и исправление набранного слова
    private fun complete(
        request: SuggestionRequest,
        boosts: Boosts,
        model: LanguageModel,
        user: UserLanguageModel,
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

        // Слово набрано правильно — предлагать похожие незачем, только дополнения.
        if (!isKnown) {
            dictionary.rankByTypo(query).forEach { (index, distance) ->
                offer(dictionary.wordAt(index), dictionary.scoreAt(index) - typoPenalty(distance), distance)
            }
        }

        // Слова, которых нет в словаре: выученные и уже написанные в этом поле.
        user.wordsWithPrefix(query).forEach { word -> offer(word, USER_WORD_SCORE, distance = 0) }
        boosts.surroundingWithPrefix(query).forEach { word -> offer(word, USER_WORD_SCORE, distance = 0) }

        val ranked = candidates.values.sortedByDescending { it.score }
        val best = ranked.firstOrNull() ?: return emptyList()
        val isAutoCorrect = request.allowsAutoCorrect && !isKnown &&
                best.distance in 1..PrefixMatcher.EDIT &&
                best.word.length <= query.length + AUTO_CORRECT_EXTRA_CHARS

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

    /** Дополнение тем длиннее, чем меньше от него пользы: длинные хвосты штрафуем. */
    private fun completionPenalty(word: String, query: String): Int =
        COMPLETION_PENALTY * (word.length - query.length).coerceAtLeast(0)

    /**
     * Цена исправления. Постоянная часть важнее переменной: слово, которое
     * начинается ровно с набранного, почти всегда лучше «похожего».
     */
    private fun typoPenalty(distance: Int): Int = TYPO_BASE_PENALTY + TYPO_PENALTY * distance
    // endregion

    // region Словарный поиск
    /** Слова, начинающиеся ровно на [query] — основной источник дополнений. */
    private fun WordDictionary.rankByPrefix(query: String): IntArray {
        val ranking = TopIndices(RANKING_SIZE)
        prefixRange(query).forEach { index ->
            ranking.offer(index, scoreAt(index) - COMPLETION_PENALTY * (lengthAt(index) - query.length))
        }
        return ranking.indices()
    }

    /**
     * Слова, отличающиеся от набранного на пару правок.
     *
     * Перебирать весь словарь незачем: слово начинается либо с той же буквы,
     * либо с её соседки по клавиатуре, либо со второй набранной буквы —
     * это покрывает промах, пропуск и перестановку в начале слова.
     */
    private fun WordDictionary.rankByTypo(query: String): List<Pair<Int, Int>> {
        if (query.length < MIN_TYPO_LENGTH) return emptyList()

        val matcher = PrefixMatcher(query, PrefixMatcher.budgetFor(query.length))
        val firstChars = KeyProximity.withNeighbors(query[0]) + query[1]
        val result = ArrayList<Pair<Int, Int>>(RANKING_SIZE)
        val ranking = TopIndices(RANKING_SIZE)
        val distances = HashMap<Int, Int>(RANKING_SIZE * 4)

        firstChars.forEach { char ->
            rangeOf(char).forEach { index ->
                if (char == query[0] && startsWithQuery(index, query)) return@forEach

                val distance = matcher.match(this, index)
                if (distance == PrefixMatcher.NO_MATCH) return@forEach

                distances[index] = distance
                ranking.offer(index, scoreAt(index) - TYPO_PENALTY * distance)
            }
        }

        ranking.indices().forEach { index -> result += index to (distances[index] ?: 0) }
        return result
    }

    private fun WordDictionary.startsWithQuery(index: Int, query: String): Boolean =
        lengthAt(index) >= query.length && (0 until query.length).all { charAt(index, it) == query[it] }
    // endregion

    /**
     * Надбавки, не зависящие от того, как слово было найдено:
     * личная частота, пары слов и лексика текущего поля.
     */
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

        /** Сколько словарных кандидатов доходит до финальной сортировки. */
        const val RANKING_SIZE = 12

        const val MIN_WORD_LENGTH = 2
        const val MIN_TYPO_LENGTH = 2

        const val SURROUNDING_LIMIT = 32

        // Веса. Базовая шкала — частота словаря, 1..1000.
        const val COMPLETION_PENALTY = 24
        const val TYPO_PENALTY = 120
        const val TYPO_BASE_PENALTY = 150
        const val SURROUNDING_BONUS = 220
        const val RECENT_BONUS = 140
        const val USER_WORD_SCORE = 500
        const val START_WORD_SCORE = 200
        const val START_WORDS = 3

        const val DICTIONARY_PAIR_WEIGHT = 8
        const val WEIGHT_UNIT = 10

        /** Насколько личная статистика может обогнать словарную частоту. */
        const val PERSONAL_WEIGHT = 110
        const val PERSONAL_LIMIT = 450

        /** Своя пара слов должна обгонять словарную уже после нескольких повторов. */
        const val PAIR_WEIGHT = 350
        const val PAIR_LIMIT = 1200

        /** На сколько букв исправление может быть длиннее набранного. */
        const val AUTO_CORRECT_EXTRA_CHARS = 1

        fun personalScore(count: Int): Int =
            if (count <= 0) 0 else minOf(PERSONAL_LIMIT, (PERSONAL_WEIGHT * ln(1.0 + count)).toInt())

        fun learnedPairScore(count: Int): Int =
            if (count <= 0) 0 else minOf(PAIR_LIMIT, (PAIR_WEIGHT * ln(1.0 + count)).toInt())

        fun String.capitalizedIf(capitalize: Boolean): String =
            if (capitalize) replaceFirstChar(Char::uppercaseChar) else this

        /**
         * Повторяет регистр набранного слова: `При` → `Привет`, `ПРИ` → `ПРИВЕТ`.
         */
        fun String.matchCaseOf(typed: String): String = when {
            typed.length > 1 && typed.all { !it.isLetter() || it.isUpperCase() } -> uppercase()
            typed.firstOrNull()?.isUpperCase() == true -> capitalizedIf(true)
            else -> this
        }
    }
}
