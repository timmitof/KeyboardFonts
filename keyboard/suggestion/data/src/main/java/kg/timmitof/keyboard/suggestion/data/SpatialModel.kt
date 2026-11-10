package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.keyboard.suggestion.domain.model.KeyGeometry
import kg.timmitof.keyboard.suggestion.domain.model.KeyPoint
import kg.timmitof.keyboard.suggestion.domain.model.TypedKey
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

/**
 * Насколько опечатка похожа на промах пальцем.
 *
 * Замена буквы. Палец ложится вокруг центра клавиши
 * нормальным разбросом, поэтому вероятность «хотел нажать K, попал сюда» — гауссиана от расстояния
 * до центра K. Отношение её к той же величине для нажатой клавиши и есть бонус замене:
 * ткнул в край «к» у самой «е» — замена на «е» почти бесплатна, попал в центр «к» — почти не помогает.
 *
 * Без точек касания (ввод не с клавиш, черновик разошёлся с текстом) — прежнее правило:
 * соседние клавиши получают постоянный бонус.
 *
 * Лишняя буква: палец стукнул клавишу дважды («приввет») или задел соседнюю, нажимая букву рядом
 * («привнет» — «н» у самой «в»). Пропущенная — только удвоенная буква, нажатая один раз («клас»):
 * у потерянного касания точки нет, и о других пропусках пальцы ничего не говорят.
 */
internal class SpatialModel private constructor(
    private val geometry: KeyGeometry?,
    private val query: String,
    private val touches: Array<KeyPoint>?,
) {

    /** Бонус исправлению [word]: замены, одна лишняя или одна пропущенная буква. Иные правки — 0. */
    fun bonus(word: String): Int {
        if (geometry == null) return 0

        return when (word.length - query.length) {
            0 -> substitutionsBonus(word)
            -1 -> extraLetterBonus(word)
            1 -> missedLetterBonus(word)
            else -> 0
        }
    }

    /**
     * Бонус слову, чьё начало — исправление набранного. Начало берём той длины, что даёт лучший бонус:
     * при лишней букве оно на одну короче набранного, при пропущенной — на одну длиннее.
     */
    fun prefixBonus(word: String): Int =
        PREFIX_LENGTH_SHIFTS.maxOf { shift -> bonus(word.take(query.length + shift)) }

    /** Больше [MAX_SUBSTITUTIONS] замен — уже не промах пальцем, бонуса нет. */
    private fun substitutionsBonus(word: String): Int {
        var mismatches = 0
        var total = 0
        query.indices.forEach { index ->
            if (query[index] == word[index]) return@forEach
            if (++mismatches > MAX_SUBSTITUTIONS) return 0
            total += substitutionBonus(index, word[index])
        }
        return total
    }

    /**
     * Начала слова, где буквы заменены соседними, в которые палец почти попал: касание легло
     * между клавишами, и точный префикс может оказаться не тем. Самые вероятные первыми, без самого [query].
     * Без точек касания вариантов нет: по центру клавиши гадать не о чем.
     */
    fun prefixVariants(): List<String> {
        val geometry = geometry ?: return emptyList()
        if (touches == null || query.length < MIN_VARIANT_LENGTH) return emptyList()

        var beams = listOf(Variant(query, likelihood = 1f, substitutions = 0))
        query.indices.forEach { index ->
            val alternatives = geometry.letters.mapNotNull { char ->
                if (char == query[index]) return@mapNotNull null
                likelihood(index, char)?.takeIf { it >= MIN_VARIANT_LIKELIHOOD }?.let { char to it }
            }
            if (alternatives.isEmpty()) return@forEach

            val expanded = beams.filter { it.substitutions < MAX_SUBSTITUTIONS }.flatMap { beam ->
                alternatives.map { (char, likelihood) -> beam.replaced(index, char, likelihood) }
            }
            beams = (beams + expanded).sortedByDescending { it.likelihood }.take(MAX_VARIANTS + 1)
        }
        return beams.filter { it.substitutions > 0 }.map { it.prefix }
    }

    /** Лишняя буква может стоять в нескольких местах («приввет»): берём лучшее объяснение. */
    private fun extraLetterBonus(word: String): Int {
        var best = 0
        query.indices.forEach { index ->
            val isRemovable = query.regionMatches(0, word, 0, index) &&
                    query.regionMatches(index + 1, word, index, word.length - index)
            if (isRemovable) best = maxOf(best, extraTouchBonus(index))
        }
        return best
    }

    /** Лишнее касание [index] — повтор соседней буквы или промах по клавише рядом с ней. */
    private fun extraTouchBonus(index: Int): Int {
        val extra = query[index]
        val around = listOfNotNull(query.getOrNull(index - 1), query.getOrNull(index + 1))
        if (extra in around) return REPEAT_BONUS

        return around.maxOfOrNull { letter -> substitutionBonus(index, letter) } ?: 0
    }

    /** Пропущена буква, стоявшая рядом с такой же: удвоенную нажали один раз. */
    private fun missedLetterBonus(word: String): Int {
        word.indices.forEach { index ->
            val isMissed = word.regionMatches(0, query, 0, index) &&
                    word.regionMatches(index + 1, query, index, query.length - index)
            if (isMissed && (word.getOrNull(index - 1) == word[index] || word.getOrNull(index + 1) == word[index])) {
                return REPEAT_BONUS
            }
        }
        return 0
    }

    private fun substitutionBonus(index: Int, intended: Char): Int {
        likelihood(index, intended)?.let { return (MAX_TOUCH_BONUS * it).roundToInt() }

        val geometry = geometry ?: return 0
        val typedCenter = geometry.centerOf(query[index]) ?: return 0
        val intendedCenter = geometry.centerOf(intended) ?: return 0
        return if (typedCenter.isNeighborOf(intendedCenter)) NEIGHBOR_BONUS else 0
    }

    /**
     * exp(-(d²(K) - d²(нажатая)) / 2): во сколько раз касание вероятнее для K, чем для нажатой клавиши,
     * не больше 1. `null` — нет точки касания или буквы на раскладке.
     */
    private fun likelihood(index: Int, intended: Char): Float? {
        val touch = touches?.get(index) ?: return null
        val geometry = geometry ?: return null
        val typedCenter = geometry.centerOf(query[index]) ?: return null
        val intendedCenter = geometry.centerOf(intended) ?: return null

        return exp((touch.spread(typedCenter) - touch.spread(intendedCenter)) / 2f).coerceAtMost(1f)
    }

    private class Variant(val prefix: String, val likelihood: Float, val substitutions: Int) {

        fun replaced(index: Int, char: Char, charLikelihood: Float) = Variant(
            prefix = StringBuilder(prefix).apply { setCharAt(index, char) }.toString(),
            likelihood = likelihood * charLikelihood,
            substitutions = substitutions + 1,
        )
    }

    /** Квадрат расстояния в единицах разброса пальца; по вертикали своя шкала — ряд выше клавиши. */
    private fun KeyPoint.spread(center: KeyPoint): Float {
        val dx = (x - center.x) / SIGMA_X
        val dy = (y - center.y) / SIGMA_Y
        return dx * dx + dy * dy
    }

    private fun KeyPoint.isNeighborOf(other: KeyPoint): Boolean =
        this != other && abs(x - other.x) < NEIGHBOR_REACH && abs(y - other.y) < NEIGHBOR_REACH

    companion object {

        /**
         * [touches] берутся, только если буква в букву совпадают с [query]: иначе черновик разошёлся
         * с текстом (курсор переставили, слово правили вставкой), и точки относятся к другим буквам.
         */
        fun of(geometry: KeyGeometry?, query: String, touches: List<TypedKey>): SpatialModel {
            val points = if (geometry != null && touches.size == query.length) {
                touches.mapIndexed { index, typed ->
                    if (typed.char.lowercaseChar().foldToDictionary() != query[index]) return SpatialModel(geometry, query, null)
                    geometry.pointOf(typed) ?: return SpatialModel(geometry, query, null)
                }.toTypedArray()
            } else {
                null
            }
            return SpatialModel(geometry, query, points)
        }

        private fun Char.foldToDictionary(): Char = if (this == 'ё') 'е' else this

        const val MAX_SUBSTITUTIONS = 2

        /** Однобуквенный префикс — полсловаря: вариантами не перебираем. */
        const val MIN_VARIANT_LENGTH = 2

        /** Касание не дальше ~четверти клавиши от границы с соседом. */
        const val MIN_VARIANT_LIKELIHOOD = 0.5f

        const val MAX_VARIANTS = 4

        /** Разброс касаний: доля ширины клавиши и высоты ряда. */
        const val SIGMA_X = 0.6f
        const val SIGMA_Y = 0.55f

        /** Центры дальше — уже не соседи (по диагонали в рядах со сдвигом — около 0,5–1). */
        const val NEIGHBOR_REACH = 1.2f

        /** Касание на самой границе: замена почти так же вероятна, как набранная буква. */
        const val MAX_TOUCH_BONUS = 220

        /** Без точки касания: соседняя клавиша. Совпадает с прежним бонусом за соседство. */
        const val NEIGHBOR_BONUS = 120

        /** Двойное касание одной клавиши или удвоенная буква, нажатая один раз. */
        const val REPEAT_BONUS = NEIGHBOR_BONUS

        private val PREFIX_LENGTH_SHIFTS = intArrayOf(-1, 0, 1)
    }
}
