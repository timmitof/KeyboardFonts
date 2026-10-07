package kg.timmitof.keyboard.data.models

/** [script] — по нему подбирается алфавит для полей, требующих латиницу (почта, пароль). */
data class KeyboardLayoutDto(
    val name: String,
    val script: String? = null,
    val largeLabels: Boolean = false,
    val rows: List<List<KeyboardKeyDto>>
)
