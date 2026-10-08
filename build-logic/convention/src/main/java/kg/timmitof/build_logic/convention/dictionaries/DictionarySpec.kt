package kg.timmitof.build_logic.convention.dictionaries

import java.io.Serializable

/**
 * Правила сборки одного словаря. [Serializable] — чтобы Gradle сравнивал их как вход задачи
 * и пересобирал словарь только при изменении правил или исходника.
 *
 * @param folds пары символов подряд: `"ёе"` — «ё» сводится к «е».
 * @param trustedWords сколько самых частых слов берём без проверки на опечатку.
 * @param typoNeighborMinScore частота соседа в одну правку, при которой редкое слово считаем опечаткой.
 * @param forms файл словоформ языка по леммам для фильтра Блума; `null` — фильтра нет.
 * @param formsMinOccurrences сколько раз форма леммы должна встретиться в частотном списке, чтобы лемма попала в фильтр.
 * @param formsFalsePositiveRate доля ложных «да» фильтра словоформ.
 */
data class DictionarySpec(
    val code: String,
    val source: String,
    val alphabet: String,
    val singleLetters: String,
    val maxWords: Int,
    val maxLength: Int,
    val folds: String,
    val trustedWords: Int,
    val typoNeighborMinScore: Int,
    val forms: String?,
    val formsMinOccurrences: Int,
    val formsFalsePositiveRate: Double,
) : Serializable {

    companion object {
        private const val serialVersionUID = 3L
    }
}
