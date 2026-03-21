package kg.timmitof.keyboard.domain.model

data class KeyboardLayout(
    val name: String,
    val rows: List<List<KeyboardKey>>
)