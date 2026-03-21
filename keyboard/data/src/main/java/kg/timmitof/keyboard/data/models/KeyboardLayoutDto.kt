package kg.timmitof.keyboard.data.models

data class KeyboardLayoutDto(
    val name: String,
    val rows: List<List<KeyboardKeyDto>>
)