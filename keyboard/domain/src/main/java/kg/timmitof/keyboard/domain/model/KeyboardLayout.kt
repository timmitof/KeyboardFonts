package kg.timmitof.keyboard.domain.model

data class KeyboardLayout(
    val name: String,
    val keyboardKeys: KeyRow
)

typealias KeyRow = List<List<KeyboardKey>>