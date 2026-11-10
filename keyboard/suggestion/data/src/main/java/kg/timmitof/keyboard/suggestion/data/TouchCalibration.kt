package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.keyboard.suggestion.domain.model.TypedKey

/**
 * Куда пользователь на самом деле попадает по клавишам. Палец и хват у всех свои: кто-то стабильно
 * бьёт ниже центра, кто-то — правее в правой половине. Средний сдвиг касания копится по каждой букве
 * из слов, набранных верно, и вычитается из новых касаний — пространственная модель считает
 * расстояния от «своего» центра клавиши, а не от нарисованного.
 *
 * Редкой букве своих замеров мало, поэтому её сдвиг тянется к общему по всем буквам, пока замеры
 * не накопятся. Среднее скользящее: привычки меняются (чехол, другая рука), старое постепенно забывается.
 * Один на все языки: сдвиг — про руку, а не про язык.
 */
internal class TouchCalibration {

    private val keys = HashMap<Char, Bias>(64)

    private val overall = Bias()

    @Volatile
    var isDirty: Boolean = false
        private set

    /** Касания слова, набранного верно: буква — та, в которую метили. */
    @Synchronized
    fun observe(touches: List<TypedKey>) {
        touches.forEach { touch ->
            val x = touch.offsetX.coerceIn(-MAX_OFFSET, MAX_OFFSET)
            val y = touch.offsetY.coerceIn(-MAX_OFFSET, MAX_OFFSET)
            keys.getOrPut(touch.char) { Bias() }.add(x, y)
            overall.add(x, y)
        }
        if (touches.isNotEmpty()) isDirty = true
    }

    /** Касания относительно личного центра клавиши. Пока замеров нет — без изменений. */
    @Synchronized
    fun correct(touches: List<TypedKey>): List<TypedKey> {
        if (touches.isEmpty() || overall.count == 0) return touches

        val overallX = overall.shrunkX(OVERALL_PRIOR)
        val overallY = overall.shrunkY(OVERALL_PRIOR)

        return touches.map { touch ->
            val bias = keys[touch.char]
            val biasX = bias?.towards(overallX, Bias::meanX) ?: overallX
            val biasY = bias?.towards(overallY, Bias::meanY) ?: overallY
            touch.copy(offsetX = touch.offsetX - biasX, offsetY = touch.offsetY - biasY)
        }
    }

    @Synchronized
    fun export(): List<String> {
        isDirty = false
        return buildList {
            add(overall.line(OVERALL_KEY))
            keys.forEach { (char, bias) -> add(bias.line(char.toString())) }
        }
    }

    @Synchronized
    fun restore(lines: Sequence<String>) {
        lines.forEach { line ->
            val parts = line.split(' ')
            if (parts.size != FIELDS) return@forEach
            val bias = Bias.parse(parts) ?: return@forEach

            when {
                parts[0] == OVERALL_KEY -> overall.set(bias)
                parts[0].length == 1 -> keys[parts[0][0]] = bias
            }
        }
    }

    /** Скользящее среднее сдвига; [count] ограничен — так новые замеры весят не меньше 1/[MAX_SAMPLES]. */
    private class Bias(var meanX: Float = 0f, var meanY: Float = 0f, var count: Int = 0) {

        fun add(x: Float, y: Float) {
            count = minOf(count + 1, MAX_SAMPLES)
            meanX += (x - meanX) / count
            meanY += (y - meanY) / count
        }

        /** Свой сдвиг, подтянутый к [prior] тем сильнее, чем меньше замеров. */
        fun towards(prior: Float, mean: (Bias) -> Float): Float =
            (mean(this) * count + prior * KEY_PRIOR) / (count + KEY_PRIOR)

        /** Общий сдвиг, подтянутый к нулю, пока замеров мало. */
        fun shrunkX(prior: Int): Float = meanX * count / (count + prior)
        fun shrunkY(prior: Int): Float = meanY * count / (count + prior)

        fun set(other: Bias) {
            meanX = other.meanX
            meanY = other.meanY
            count = other.count
        }

        fun line(key: String): String = "$key $meanX $meanY $count"

        companion object {
            fun parse(parts: List<String>): Bias? {
                val x = parts[1].toFloatOrNull() ?: return null
                val y = parts[2].toFloatOrNull() ?: return null
                val count = parts[3].toIntOrNull()?.coerceIn(0, MAX_SAMPLES) ?: return null
                return Bias(x, y, count)
            }
        }
    }

    companion object {
        private const val OVERALL_KEY = "*"
        private const val FIELDS = 4

        /** Касание дальше края клавиши — уже не про привычку, а про промах. */
        private const val MAX_OFFSET = 0.5f

        /** Окно скользящего среднего: около пары сотен последних нажатий. */
        const val MAX_SAMPLES = 200

        /** Сколько замеров буквы весят как общий сдвиг. */
        const val KEY_PRIOR = 20

        /** Пока общих замеров меньше этого, сдвиг заметно меньше намеренного — первые слова его не раскачают. */
        const val OVERALL_PRIOR = 30
    }
}
