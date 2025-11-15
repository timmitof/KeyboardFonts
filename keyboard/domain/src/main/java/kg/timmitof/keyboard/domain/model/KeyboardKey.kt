package kg.timmitof.keyboard.domain.model

data class KeyboardKey(
    val labelLower: String,
    val labelUpper: String,
    val keyLongPress: KeyLongPress,
    val type: String,
    val weight: Float
)