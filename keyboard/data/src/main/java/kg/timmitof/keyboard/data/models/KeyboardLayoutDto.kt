package kg.timmitof.keyboard.data.models

data class KeyboardLayoutDto(
    val name: String,
    val largeLabels: Boolean = false,
    val rows: List<List<KeyboardKeyDto>>
)