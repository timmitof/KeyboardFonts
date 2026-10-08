package kg.timmitof.build_logic.convention.dictionaries

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty

/**
 * Словари Т9 из частотных списков (`слово число` построчно, как в hermitdave/FrequencyWords).
 * Исходники лежат вне ассетов, в APK уходят только готовые `<code>.dict` и индекс опечаток `<code>.spell`.
 *
 * ```
 * dictionaries {
 *     language("ru_ru", source = "ru_full.txt") {
 *         alphabet = "абв…"
 *         forms = "ru_forms.txt.gz"
 *         formsMinOccurrences = 1
 *         formsFalsePositiveRate = 0.03
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

    /**
     * Сколько самых частых слов берём. Каждые 100 тыс. слов — примерно +1,5 МБ индекса опечаток `<code>.spell`
     * в APK; на телефоне он отображается в память, а не копируется в кучу.
     */
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

    /**
     * Файл словоформ языка (одна лемма в строке, формы через пробел; можно `.gz`) в той же папке, что и [source].
     * Из него собирается фильтр Блума `<code>.forms`: такие слова Т9 не заменяет автозаменой
     * и не отбрасывает как опечатки. Не задан или файла нет — фильтра нет, язык работает как раньше.
     */
    var forms: String? = null

    /**
     * Лемма попадает в фильтр [forms], только если хоть одна её форма встречается в частотном списке
     * не меньше стольких раз. Остальные леммы — редкие слова, которых в речи почти нет. `0` — все леммы.
     */
    var formsMinOccurrences: Int = DEFAULT_FORMS_MIN_OCCURRENCES

    /**
     * Доля ложных «да» фильтра [forms] — слов, ошибочно принятых за настоящие (их не исправит автозамена).
     * Каждое удвоение доли экономит около 1,4 бита на форму.
     */
    var formsFalsePositiveRate: Double = DEFAULT_FORMS_FALSE_POSITIVE_RATE

    private val folds = StringBuilder()

    /** Сводит [from] к [to] с суммированием частот — так же, как движок приводит ввод к форме словаря. */
    fun fold(from: Char, to: Char) {
        folds.append(from).append(to)
    }

    internal fun build(): DictionarySpec {
        require(alphabet.isNotEmpty()) { "dictionaries: для $code не задан alphabet" }
        require(maxWords > 0) { "dictionaries: maxWords для $code должен быть больше нуля" }
        require(formsMinOccurrences >= 0) { "dictionaries: formsMinOccurrences для $code не может быть отрицательным" }
        require(formsFalsePositiveRate > 0.0 && formsFalsePositiveRate < 1.0) {
            "dictionaries: formsFalsePositiveRate для $code должен быть в (0; 1)"
        }

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
            forms = forms,
            formsMinOccurrences = formsMinOccurrences,
            formsFalsePositiveRate = formsFalsePositiveRate,
        )
    }

    private companion object {
        const val DEFAULT_MAX_WORDS = 100_000
        const val DEFAULT_MAX_LENGTH = 24

        /** Размер прежних ручных словарей: на нём опечаток почти нет, и под него подобраны веса подсказок. */
        const val DEFAULT_TRUSTED_WORDS = 40_000
        const val DEFAULT_TYPO_NEIGHBOR_MIN_SCORE = 600

        const val DEFAULT_FORMS_MIN_OCCURRENCES = 1
        const val DEFAULT_FORMS_FALSE_POSITIVE_RATE = 0.03
    }
}
