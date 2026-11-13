package kg.timmitof.feature_settings.domain.model

import kg.timmitof.keyboard.translate.domain.model.ModelState
import kg.timmitof.keyboard.translate.domain.model.TranslationLanguage

/**
 * Языки для вкладки «Переводчик».
 *
 * @param languages языки пользователя: сначала по умолчанию, потом остальные скачанные и качающиеся.
 * @param catalog все языки переводчика — для шторки «Добавить язык перевода».
 */
data class TranslatorOverview(
    val languages: List<TranslatorLanguageItem> = emptyList(),
    val catalog: List<TranslatorLanguageItem> = emptyList(),
)

/** @param isBuiltIn язык встроен в переводчик (английский): его не качают и не удаляют. */
data class TranslatorLanguageItem(
    val language: TranslationLanguage,
    val state: ModelState,
    val isBuiltIn: Boolean,
)
