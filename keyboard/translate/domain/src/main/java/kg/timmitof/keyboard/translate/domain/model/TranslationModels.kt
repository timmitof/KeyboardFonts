package kg.timmitof.keyboard.translate.domain.model

/**
 * Модели языков: какие уже на телефоне и какие стоят в очереди на загрузку (качаются или ждут сеть).
 */
data class TranslationModels(
    val downloaded: Set<String> = emptySet(),
    val downloading: Set<String> = emptySet(),
) {
    fun stateOf(code: String): ModelState = when (code) {
        in downloaded -> ModelState.DOWNLOADED
        in downloading -> ModelState.DOWNLOADING
        else -> ModelState.MISSING
    }
}

enum class ModelState { DOWNLOADED, DOWNLOADING, MISSING }
