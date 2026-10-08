package kg.timmitof.build_logic.convention.languages

import kg.timmitof.build_logic.convention.dictionaries.DictionarySpec
import java.io.Serializable

/**
 * Один язык каталога. [Serializable] — чтобы Gradle сравнивал каталог как вход задач
 * и пересобирал `languages.json` и словари только при его изменении.
 *
 * @param layout имя файла раскладки в `assets/layouts/` без `.json`.
 * @param alphabet буквы языка в нижнем регистре; по нему же собирается словарь Т9.
 * @param dictionary правила словаря Т9; `null` — язык без подсказок и автозамены.
 */
data class LanguageSpec(
    val code: String,
    val name: String,
    val shortName: String,
    val isLatin: Boolean,
    val layout: String,
    val alphabet: String,
    val dictionary: DictionarySpec?,
) : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }
}
