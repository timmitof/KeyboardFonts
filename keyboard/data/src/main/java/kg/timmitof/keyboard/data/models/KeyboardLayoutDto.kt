package kg.timmitof.keyboard.data.models

/**
 * @param script письменность раскладки — по ней подбирается алфавит для полей,
 * которые латиницу требуют (адрес почты, пароль).
 */
data class KeyboardLayoutDto(
    val name: String,
    val script: String? = null,
    val largeLabels: Boolean = false,
    val rows: List<List<KeyboardKeyDto>>
)
