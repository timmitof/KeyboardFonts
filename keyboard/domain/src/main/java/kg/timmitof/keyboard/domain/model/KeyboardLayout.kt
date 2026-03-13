package kg.timmitof.keyboard.domain.model

data class KeyboardLayout(
    val name: String,
    val rows: KeyRow
)

typealias KeyRow = List<List<KeyboardKey>>?