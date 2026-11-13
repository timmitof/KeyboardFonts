package kg.timmitof.keyboard.translate.domain.model

/**
 * Что переводчик помнит между открытиями.
 *
 * @param targetCode язык, на который переводили в прошлый раз; null — ещё не выбирали.
 * @param isStyled применять ли выбранный шрифт к переводу (кнопка «Aa»).
 */
data class TranslatorPreferences(
    val targetCode: String? = null,
    val isStyled: Boolean = true,
)
