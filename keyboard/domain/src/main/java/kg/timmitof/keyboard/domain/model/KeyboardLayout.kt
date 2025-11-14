package kg.timmitof.keyboard.domain.model

data class KeyboardLayout(
    val name: String,
    val keyRows: List<List<KeyRow>>
)