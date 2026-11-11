package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.keyboard.suggestion.domain.model.KeyGeometry
import kg.timmitof.keyboard.suggestion.domain.model.TypedKey
import kotlin.math.abs
import kotlin.math.exp

/**
 * Умные зоны клавиш (Predictive Adaptive Touch Targets): букву выбирает не только палец, но и слово.
 * Набрано «пр», палец лёг на край «ш» у самой «и» — «прш» не начинает ни одно слово, «при» — сотни:
 * печатается «и». Зоны невидимы и меняются от буквы к букве.
 *
 * Осторожно, потому что ошибку видно сразу (нажал одно — напечаталось другое):
 * - центр клавиши — якорь: касание ближе [ANCHOR] к центру по обеим осям никогда не подменяется;
 * - сосед должен быть почти так же вероятен по касанию ([MIN_LIKELIHOOD]);
 * - и в [MIN_ADVANTAGE] раз вероятнее по словарю с учётом касания.
 */
internal object AdaptiveKeys {

    /**
     * @param prefix начало слова в словарной форме, без нажатой буквы.
     * @param touch нажатая буква — для точки на клавиатуре; [calibrated] — она же относительно личного центра клавиш.
     * @return сосед с тем же касанием относительно его клавиши или `null`.
     */
    fun resolve(
        model: LanguageModel,
        user: UserLanguageModel,
        geometry: KeyGeometry,
        prefix: String,
        touch: TypedKey,
        calibrated: TypedKey,
    ): TypedKey? {
        if (abs(calibrated.offsetX) < ANCHOR && abs(calibrated.offsetY) < ANCHOR) return null
        if (!prefix.all(Char::isLetter)) return null

        val point = geometry.pointOf(calibrated) ?: return null
        val typed = touch.char

        var best: Char? = null
        var bestScore = prefixWeight(model, user, prefix + typed.fold()) * MIN_ADVANTAGE

        geometry.letters.forEach { letter ->
            if (letter == typed) return@forEach
            val likelihood = SpatialModel.likelihood(geometry, point, typed, letter) ?: return@forEach
            if (likelihood < MIN_LIKELIHOOD) return@forEach

            val score = likelihood * prefixWeight(model, user, prefix + letter.fold())
            if (score > bestScore) {
                best = letter
                bestScore = score
            }
        }

        // Возвращаем сырое касание: личная калибровка применится к нему позже, вместе с остальными буквами.
        val raw = geometry.pointOf(touch) ?: return null
        return best?.let { geometry.touchOn(it, raw) }
    }

    /**
     * Насколько часто слова начинаются с [prefix]: сумма частот словаря, восстановленных из очков
     * (очки — логарифм числа вхождений). Свои слова пользователя весят как частое слово.
     */
    private fun prefixWeight(model: LanguageModel, user: UserLanguageModel, prefix: String): Double {
        val dictionary = model.dictionary
        var weight = 0.0
        dictionary.prefixRange(prefix).forEach { index ->
            weight += exp(dictionary.scoreAt(index) * SCORE_TO_LOG)
        }
        if (user.wordsWithPrefix(prefix).isNotEmpty()) weight += exp(USER_WORD_SCORE * SCORE_TO_LOG)
        return weight
    }

    private fun Char.fold(): Char = if (this == 'ё') 'е' else this

    /** Доля размера клавиши от центра: ближе — никогда не подменяем. Зоны намеренно узкие — край в 15 %. */
    const val ANCHOR = 0.35f

    /** Касание для соседа хотя бы на 60 % так же вероятно, как для нажатой: это и есть «у самого края». */
    const val MIN_LIKELIHOOD = 0.6f

    /** Во сколько раз сосед должен перевешивать: слабый перевес не стоит неожиданной буквы. */
    const val MIN_ADVANTAGE = 3.0

    /** Очки 1..1000 — `1000·ln(n)/ln(max)`; у частотных списков ln(max) ≈ 16. */
    private const val SCORE_TO_LOG = 16.0 / 1000

    private const val USER_WORD_SCORE = 500
}
