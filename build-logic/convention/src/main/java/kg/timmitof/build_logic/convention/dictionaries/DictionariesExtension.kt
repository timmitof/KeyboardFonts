package kg.timmitof.build_logic.convention.dictionaries

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty

/**
 * Словари Т9 из частотных списков (`слово число` построчно, как в hermitdave/FrequencyWords).
 * Исходники лежат вне ассетов, в APK уходит только готовый `<code>.dict`.
 *
 * ```
 * dictionaries {
 *     language("ru_ru", source = "ru_full.txt") {
 *         alphabet = "абв…"
 *         fold('ё', 'е')
 *     }
 * }
 * ```
 */
abstract class DictionariesExtension {

    /** Папка с исходными списками; по умолчанию `dictionaries/` модуля. */
    abstract val sourceDirectory: DirectoryProperty

    /** Заполняется через [language]; напрямую не трогаем. */
    abstract val languages: ListProperty<DictionarySpec>

    /** Язык без исходника пропускается — пока списка нет, работает словарь из ассетов. */
    fun language(code: String, source: String, configure: DictionarySpecBuilder.() -> Unit) {
        languages.add(DictionarySpecBuilder(code, source).apply(configure).build())
    }
}

class DictionarySpecBuilder internal constructor(
    private val code: String,
    private val source: String,
) {

    /** Допустимые символы слова, в нижнем регистре. Остальные слова (цифры, латиница в русском) отбрасываются. */
    var alphabet: String = ""

    /** Однобуквенные слова, которые оставляем (предлоги, союзы); прочие одиночные буквы — шум субтитров. */
    var singleLetters: String = ""

    /** Сколько самых частых слов берём. Каждые 100 тыс. слов — примерно +55 МБ памяти индекса опечаток. */
    var maxWords: Int = DEFAULT_MAX_WORDS

    /** Длиннее — почти всегда склейки и мусор; совпадает с пределом обучаемых слов. */
    var maxLength: Int = DEFAULT_MAX_LENGTH

    /**
     * Сколько самых частых слов берём как есть. Ниже по частоте в корпус субтитров просачиваются
     * повторяющиеся опечатки (`teh`, `етого`): попав в словарь, они перестали бы исправляться.
     */
    var trustedWords: Int = DEFAULT_TRUSTED_WORDS

    /**
     * Слово за пределами [trustedWords] отбрасывается, если в одну правку от него есть слово
     * с частотой не ниже этой (шкала 1..1000). То же правило, что у обучения на вводе.
     */
    var typoNeighborMinScore: Int = DEFAULT_TYPO_NEIGHBOR_MIN_SCORE

    private val folds = StringBuilder()

    /** Сводит [from] к [to] с суммированием частот — так же, как движок приводит ввод к форме словаря. */
    fun fold(from: Char, to: Char) {
        folds.append(from).append(to)
    }

    internal fun build(): DictionarySpec {
        require(alphabet.isNotEmpty()) { "dictionaries: для $code не задан alphabet" }
        require(maxWords > 0) { "dictionaries: maxWords для $code должен быть больше нуля" }

        return DictionarySpec(
            code = code,
            source = source,
            alphabet = alphabet,
            singleLetters = singleLetters,
            maxWords = maxWords,
            maxLength = maxLength,
            folds = folds.toString(),
            trustedWords = trustedWords,
            typoNeighborMinScore = typoNeighborMinScore,
        )
    }

    private companion object {
        const val DEFAULT_MAX_WORDS = 100_000
        const val DEFAULT_MAX_LENGTH = 24

        /** Размер прежних ручных словарей: на нём опечаток почти нет, и под него подобраны веса подсказок. */
        const val DEFAULT_TRUSTED_WORDS = 40_000
        const val DEFAULT_TYPO_NEIGHBOR_MIN_SCORE = 600
    }
}
