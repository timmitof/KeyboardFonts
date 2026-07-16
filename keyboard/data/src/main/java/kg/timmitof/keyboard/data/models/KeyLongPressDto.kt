package kg.timmitof.keyboard.data.models

data class KeyLongPressDto(
    val type: String,
    val symbols: List<String>? = null,
    val characters: List<LongPressCharacterDto>? = null
)

data class LongPressCharacterDto(
    val labelLower: String,
    val labelUpper: String? = null
)