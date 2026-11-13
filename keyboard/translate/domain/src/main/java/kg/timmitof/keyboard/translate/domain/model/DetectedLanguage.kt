package kg.timmitof.keyboard.translate.domain.model

/**
 * Язык, на котором, похоже, написан текст.
 *
 * @param code базовый тег языка без письменности и региона: `ru`, `ky`, `uz` (не `uz-Latn`).
 * @param confidence уверенность 0..1 — что весь текст на этом языке.
 */
data class DetectedLanguage(
    val code: String,
    val confidence: Float,
)
