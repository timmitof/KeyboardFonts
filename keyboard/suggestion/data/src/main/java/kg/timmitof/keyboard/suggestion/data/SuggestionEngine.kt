package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.keyboard.data.font.FontDecoder
import kg.timmitof.keyboard.suggestion.domain.model.SuggestionRequest
import kg.timmitof.keyboard.suggestion.domain.model.WordSuggestion
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
 * Пока слово набирается, к этому добавляется цена опечаток: через [SpellCorrector]
 * (SymSpell) находятся похожие слова без полного перебора словаря.
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
            model.bigrams.after(previous, PREDICTION_FOLLOWERS).forEach { follower ->
                offer(follower.word)
            }
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
     * Слова, которых ждёт продолжение фразы, среди начатых на [query].
     *
     * Берём их из пар слов — словарных и выученных — и сразу отдаём в общий
     * отбор: у них своя надбавка за связь с предыдущим словом, и без неё
     * длинное «делать» никогда не обгонит короткое частотное «да».
     *
     * @return сами эти слова — по ним видно, что дополнение подсказано контекстом,
     * а не просто угадано по началу.
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
     * Можно ли молча подставить кандидата вместо набранного при пробеле.
     *
     * Исправление опечатки — как раньше: слово почти той же длины, пара правок.
     * Дополнение начатого слова разрешаем в двух случаях: набрано достаточно,
     * чтобы догадка была уверенной («прив» → «привет»), либо продолжение прямо
     * следует из фразы («как д» → «дела») — тогда хватает и одной буквы.
     */
    private fun Candidate.replaces(query: String, expected: Set<String>): Boolean = when {
        distance > 0 -> distance <= MAX_AUTO_CORRECT_DISTANCE &&
                word.length <= query.length + AUTO_CORRECT_EXTRA_CHARS

        else -> word in expected || query.length >= AUTO_COMPLETE_MIN_CHARS
    }

    /**
     * Слово с большой буквы посреди предложения — имя или сокращение,
     * которое человек написал осознанно. Такое не исправляем.
     */
    private fun String.isCorrectable(isSentenceStart: Boolean): Boolean =
        isSentenceStart || firstOrNull()?.isUpperCase() != true

    /** Дополнение тем длиннее, чем меньше от него пользы: длинные хвосты штрафуем. */
    private fun completionPenalty(word: String, query: String): Int =
        COMPLETION_PENALTY * (word.length - query.length).coerceAtLeast(0)

    /**
     * Цена исправления. Постоянная часть важнее переменной: слово, которое
     * начинается ровно с набранного, почти всегда лучше «похожего».
     */
    private fun typoPenalty(distance: Int): Int = TYPO_BASE_PENALTY + TYPO_PENALTY * distance

    /**
     * Для коротких обрывков исправление опаснее пользы: точное дополнение уже
     * есть в префиксном поиске. Вторую правку разрешаем только для длинных слов.
     */
    private fun maxTypoDistance(query: String): Double? = when {
        query.length < MIN_TYPO_LENGTH -> null
        query.length < TWO_EDITS_MIN_LENGTH -> 1.0
        else -> SpellCorrector.MAX_EDIT_DISTANCE
    }

    /** Один промах по соседней клавише лучше случайной замены той же стоимости. */
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

        /** С коротким префиксом ждём следующую букву, а не исправляем его. */
        const val MIN_TYPO_LENGTH = 3

        /** Вторая правка допустима только для длинных слов. */
        const val TWO_EDITS_MIN_LENGTH = 8

        /** Автоисправление срабатывает только при расстоянии в одну правку. */
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

        /**
         * Насколько пара слов языка весомее частоты самого слова.
         *
         * Больше единицы намеренно: связь с предыдущим словом — самый сильный
         * сигнал из всех, что есть у клавиатуры без модели языка целиком.
         */
        const val DICTIONARY_PAIR_WEIGHT = 15
        const val WEIGHT_UNIT = 10

        /** Насколько личная статистика может обогнать словарную частоту. */
        const val PERSONAL_WEIGHT = 110
        const val PERSONAL_LIMIT = 450

        /** Своя пара слов должна обгонять словарную уже после нескольких повторов. */
        const val PAIR_WEIGHT = 700
        const val PAIR_LIMIT = 2000

        /** На сколько букв исправление может быть длиннее набранного. */
        const val AUTO_CORRECT_EXTRA_CHARS = 1

        /** Со скольких букв клавиатура сама дописывает слово, если контекст молчит. */
        const val AUTO_COMPLETE_MIN_CHARS = 3

        /** Сколько продолжений фразы разбирается на предсказании следующего слова. */
        const val PREDICTION_FOLLOWERS = 24

        const val EXPECTED_CAPACITY = 8

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
