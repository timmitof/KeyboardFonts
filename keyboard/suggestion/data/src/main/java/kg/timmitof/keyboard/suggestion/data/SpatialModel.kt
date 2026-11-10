package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.keyboard.suggestion.domain.model.KeyGeometry
import kg.timmitof.keyboard.suggestion.domain.model.KeyPoint
import kg.timmitof.keyboard.suggestion.domain.model.TypedKey
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

/**
 * Насколько замена буквы похожа на промах пальцем. Палец ложится вокруг центра клавиши
 * нормальным разбросом, поэтому вероятность «хотел нажать K, попал сюда» — гауссиана от расстояния
 * до центра K. Отношение её к той же величине для нажатой клавиши и есть бонус замене:
 * ткнул в край «к» у самой «е» — замена на «е» почти бесплатна, попал в центр «к» — почти не помогает.
 *
 * Без точек касания (ввод не с клавиш, черновик разошёлся с текстом) — прежнее правило:
 * соседние клавиши получают постоянный бонус.
 */
internal class SpatialModel private constructor(
    private val geometry: KeyGeometry?,
    private val query: String,
    private val touches: Array<KeyPoint>?,
) {

    /**
     * Бонус слову той же длины, что набранное, за буквы, заменённые соседними.
     * Больше [MAX_SUBSTITUTIONS] замен — уже не промах пальцем, бонуса нет.
     */
    fun bonus(word: String): Int {
        if (geometry == null || word.length != query.length) return 0

        var mismatches = 0
        var total = 0
        query.indices.forEach { index ->
            if (query[index] == word[index]) return@forEach
            if (++mismatches > MAX_SUBSTITUTIONS) return 0
            total += substitutionBonus(index, word[index])
        }
        return total
    }

    private fun substitutionBonus(index: Int, intended: Char): Int {
        val geometry = geometry ?: return 0
        val typedCenter = geometry.centerOf(query[index]) ?: return 0
        val intendedCenter = geometry.centerOf(intended) ?: return 0

        val touch = touches?.get(index) ?: return if (typedCenter.isNeighborOf(intendedCenter)) NEIGHBOR_BONUS else 0

        // exp(-(d²(K) - d²(нажатая)) / 2): во сколько раз касание вероятнее для K, чем для нажатой клавиши.
        val likelihood = exp((touch.spread(typedCenter) - touch.spread(intendedCenter)) / 2f)
        return (MAX_TOUCH_BONUS * likelihood.coerceAtMost(1f)).roundToInt()
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

        /** Разброс касаний: доля ширины клавиши и высоты ряда. */
        const val SIGMA_X = 0.6f
        const val SIGMA_Y = 0.55f

        /** Центры дальше — уже не соседи (по диагонали в рядах со сдвигом — около 0,5–1). */
        const val NEIGHBOR_REACH = 1.2f

        /** Касание на самой границе: замена почти так же вероятна, как набранная буква. */
        const val MAX_TOUCH_BONUS = 220

        /** Без точки касания: соседняя клавиша. Совпадает с прежним бонусом за соседство. */
        const val NEIGHBOR_BONUS = 120
    }
}
