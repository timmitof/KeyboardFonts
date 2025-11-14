package kg.timmitof.keyboard.domain.model

data class KeyRow(
    val labelLower: String,
    val labelUpper: String,
    val keyLongPress: KeyLongPress,
    val type: String,
    val weight: String
)