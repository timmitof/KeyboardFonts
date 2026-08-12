package kg.timmitof.keyboard.data.models

data class KeyboardKeyDto(
    val type: String,
    val weight: Float,
    val labelLower: String? = null,
    val labelUpper: String? = null,
    val subLabel: String? = null,
    val hint: String? = null,
    val output: String? = null,
    val special: Boolean = false,
    val longPress: KeyLongPressDto? = null
)
