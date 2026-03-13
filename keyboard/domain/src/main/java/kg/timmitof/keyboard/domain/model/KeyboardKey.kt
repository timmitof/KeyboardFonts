package kg.timmitof.keyboard.domain.model

data class KeyboardKey(
    val type: String,
    val weight: Float,
    val labelLower: String? = null,
    val labelUpper: String? = null,
    val longPress: KeyLongPress,
)