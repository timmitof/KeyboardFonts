package kg.timmitof.build_logic.convention.languages

import kg.timmitof.build_logic.convention.dictionaries.DictionarySpecBuilder
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.kotlin.dsl.findByType

@DslMarker
annotation class LanguagesDsl

/**
 * Каталог языков клавиатуры — единственный источник правды. Из него собираются
 * `assets/languages.json` (`keyboardfonts.languageCatalog`) и словари Т9 (`keyboardfonts.dictionaries`).
 * Порядок записей — порядок каталога, первая — запасной язык по умолчанию.
 *
 * ```
 * keyboardLanguages {
 *     language("ru_ru") {
 *         name = "Русский"
 *         alphabet = "абв…"
 *         dictionary(source = "ru_full.txt") {
 *             singleLetters = "авикосуя"
 *             fold('ё', 'е')
 *         }
 *     }
 * }
 * ```
 */
@LanguagesDsl
abstract class KeyboardLanguagesExtension {

    /** Заполняется через [language]; напрямую не трогаем. */
    abstract val languages: ListProperty<LanguageSpec>

    private val codes = mutableSetOf<String>()

    fun language(code: String, configure: LanguageSpecBuilder.() -> Unit) {
        if (!codes.add(code)) throw GradleException("keyboardLanguages: язык $code описан дважды")
        languages.add(LanguageSpecBuilder(code).apply(configure).build())
    }

    companion object {
        const val NAME = "keyboardLanguages"
    }
}

@LanguagesDsl
class LanguageSpecBuilder internal constructor(private val code: String) {

    /** Название на самом языке — так его ищут в списке («Русский», «English»). */
    var name: String = ""

    /** Подпись на пробеле и в списках; по умолчанию — язык из кода заглавными (`ru_ru` → `RU`). */
    var shortName: String = code.substringBefore('_').uppercase()

    /** Латиница нужна полям вроде почты и пароля: туда клавиатура подставит первый латинский язык. */
    var isLatin: Boolean = false

    /** Раскладка `assets/layouts/<layout>.json`; по умолчанию совпадает с кодом. */
    var layout: String = code

    /** Буквы языка в нижнем регистре; слова словаря с другими символами отбрасываются. */
    var alphabet: String = ""

    private var dictionary: Pair<String, DictionarySpecBuilder.() -> Unit>? = null

    /** Словарь Т9 из частотного списка `dictionaries/<source>` модуля подсказок. Без него язык работает без подсказок. */
    fun dictionary(source: String, configure: DictionarySpecBuilder.() -> Unit = {}) {
        dictionary = source to configure
    }

    internal fun build(): LanguageSpec {
        require(name.isNotBlank()) { "keyboardLanguages: для $code не задан name" }
        require(alphabet.isNotEmpty()) { "keyboardLanguages: для $code не задан alphabet" }

        return LanguageSpec(
            code = code,
            name = name,
            shortName = shortName,
            isLatin = isLatin,
            layout = layout,
            alphabet = alphabet,
            dictionary = dictionary?.let { (source, configure) ->
                DictionarySpecBuilder(code, source, alphabet).apply(configure).build()
            },
        )
    }
}

/** Каталог описан в одном из родительских модулей (`:keyboard`) — ищем вверх по дереву проектов. */
internal fun Project.keyboardLanguages(): KeyboardLanguagesExtension =
    generateSequence(this) { it.parent }
        .firstNotNullOfOrNull { it.extensions.findByType<KeyboardLanguagesExtension>() }
        ?: throw GradleException(
            "$path: каталог языков не найден — подключите keyboardfonts.languages в родительском модуле"
        )
