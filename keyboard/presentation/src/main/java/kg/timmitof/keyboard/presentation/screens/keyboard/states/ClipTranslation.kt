package kg.timmitof.keyboard.presentation.screens.keyboard.states

import androidx.compose.runtime.Immutable
import kg.timmitof.keyboard.translate.domain.model.TranslationLanguage

/**
 * Перевод только что скопированного текста — карточка над клавишами.
 *
 * @param source язык скопированного: на нём «Ответить на …» откроет переводчик.
 */
@Immutable
internal data class ClipTranslation(
    val original: String,
    val translation: String,
    val source: TranslationLanguage,
)
