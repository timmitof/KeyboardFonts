package kg.timmitof.keyboard.data.models

/** Формат `languages.json`; поля совпадают с тем, что пишет `GenerateLanguageCatalogTask`. */
class LanguageCatalogDto(
    val languages: List<LanguageDto>? = null,
)

class LanguageDto(
    val code: String = "",
    val name: String = "",
    val shortName: String = "",
    val isLatin: Boolean = false,
    val layout: String = "",
    val alphabet: String = "",
    val hasDictionary: Boolean = false,
)
