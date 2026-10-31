package kg.timmitof.keyboard.data.models

import kotlinx.serialization.Serializable

/** Формат `languages.json`; поля совпадают с тем, что пишет `GenerateLanguageCatalogTask`. */
@Serializable
class LanguageCatalogDto(
    val languages: List<LanguageDto>? = null,
)

@Serializable
class LanguageDto(
    val code: String = "",
    val name: String = "",
    val shortName: String = "",
    val isLatin: Boolean = false,
    val layout: String = "",
    val alphabet: String = "",
    val hasDictionary: Boolean = false,
)
