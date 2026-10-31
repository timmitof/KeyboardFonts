package kg.timmitof.keyboard.data.models

import kotlinx.serialization.Serializable

@Serializable
data class KeyLongPressDto(
    val type: String,
    val symbols: List<String>? = null,
    val characters: List<LongPressCharacterDto>? = null
)

@Serializable
data class LongPressCharacterDto(
    val labelLower: String,
    val labelUpper: String? = null
)