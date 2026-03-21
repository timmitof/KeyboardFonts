package kg.timmitof.keyboard.data.models

data class KeyLongPressDto(
    val type: String,
    val symbols: List<String>? = null  // nullable!
)