package kg.timmitof.keyboard.data.models

data class KeyboardKeyDto(
    val type: String,
    val weight: Float,
    val labelLower: String? = null,
    val labelUpper: String? = null,
    val longPress: KeyLongPressDto? = null
)