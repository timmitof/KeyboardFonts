package kg.timmitof.build_logic.convention.dictionaries

import kotlin.math.abs

/**
 * Узнаёт слова в одной правке (замена, вставка, удаление, перестановка соседних букв) от частых.
 * Индекс — как в SymSpell: частое слово и все его варианты без одной буквы. Совпадение вариантов
 * ещё не значит одну правку («xbc»/«bcx»), поэтому кандидат проверяется точно.
 */
internal class TypoFilter(frequentWords: Collection<String>) {

    private val index = HashMap<String, MutableList<String>>(frequentWords.size * 8)

    init {
        frequentWords.forEach { word ->
            word.variants().forEach { variant -> index.getOrPut(variant) { ArrayList(2) } += word }
        }
    }

    fun isTypo(word: String): Boolean =
        word.variants().any { variant ->
            index[variant]?.any { frequent -> isOneEdit(word, frequent) } == true
        }

    /** Само слово и все варианты без одной буквы. */
    private fun String.variants(): Sequence<String> =
        sequenceOf(this) + indices.asSequence().map { removeRange(it, it + 1) }

    private fun isOneEdit(a: String, b: String): Boolean {
        if (a == b || abs(a.length - b.length) > 1) return false
        if (a.length != b.length) {
            val (long, short) = if (a.length > b.length) a to b else b to a
            val diff = short.indices.firstOrNull { long[it] != short[it] } ?: return true
            return long.regionMatches(diff + 1, short, diff, short.length - diff)
        }

        val diff = a.indices.first { a[it] != b[it] }
        if (a.regionMatches(diff + 1, b, diff + 1, a.length - diff - 1)) return true

        // Перестановка соседних букв: «teh» ↔ «the».
        return diff + 1 < a.length &&
                a[diff] == b[diff + 1] && a[diff + 1] == b[diff] &&
                a.regionMatches(diff + 2, b, diff + 2, a.length - diff - 2)
    }
}
