package kg.timmitof.keyboard.data.models

import kotlinx.serialization.Serializable

/** [script] — по нему подбирается алфавит для полей, требующих латиницу (почта, пароль). */
@Serializable
data class KeyboardLayoutDto(
    val name: String,
    val script: String? = null,
    val largeLabels: Boolean = false,
    val rows: List<List<KeyboardKeyDto>>
)
