package kg.timmitof.keyboard.translate.domain.model

/**
 * Что переводчик помнит между открытиями.
 *
 * @param targetCode язык, на который переводили в прошлый раз; null — ещё не выбирали.
 * @param appTargets язык перевода по приложению (пакет → код): в каждом чате — своя пара.
 * @param recentTargets недавние языки перевода, свежие первыми: чипы над списком языков.
 * @param isAutoSource исходный язык перевода определяется по набранному тексту, а не по раскладке.
 * @param isHintShown подсказку «Новое: переводчик» уже показали (или переводчик уже открывали).
 */
data class TranslatorPreferences(
    val targetCode: String? = null,
    val appTargets: Map<String, String> = emptyMap(),
    val recentTargets: List<String> = emptyList(),
    val isAutoSource: Boolean = false,
    val isHintShown: Boolean = false,
) {

    /** Язык перевода для приложения: свой, если там уже переводили, иначе последний выбранный. */
    fun targetFor(packageName: String?): String? = packageName?.let(appTargets::get) ?: targetCode
}
