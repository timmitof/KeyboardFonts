package kg.timmitof.keyboard.presentation.screens.keyboard.states

import androidx.compose.runtime.Immutable
import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.translate.domain.model.TranslationLanguage
import kg.timmitof.keyboard.translate.domain.model.TranslationModels

/**
 * Открытая панель перевода. Ввод клавиш идёт в [editor], а в поле приложения — только перевод
 * (областью компоновки, чтобы его можно было заменять, пока набираешь).
 *
 * @param source язык раскладки в переводчике; null — переводчик его не знает (кыргызский, казахский…).
 * @param layouts включённые раскладки по коду языка перевода: на них можно поменять пару местами.
 * @param translatedFor запрос, для которого готов [translation]; отличается от [request] — перевод в пути.
 * @param revision растёт, когда тот же текст нужно перевести заново (скачали язык).
 * @param needsLeadingSpace в поле перед курсором уже есть текст без пробела — перевод отделяем пробелом.
 */
@Immutable
internal data class TranslatorState(
    val source: TranslationLanguage?,
    val target: TranslationLanguage,
    val languages: List<TranslationLanguage>,
    val layouts: Map<String, KeyboardLanguage> = emptyMap(),
    /** Скачанные и стоящие в очереди модели: лист языков и кнопка «Скачать» показывают их состояние. */
    val models: TranslationModels = TranslationModels(),
    val editor: InlineEditor = InlineEditor(),
    val translation: String = "",
    val translatedFor: TranslationRequest? = null,
    val status: TranslatorStatus = TranslatorStatus.READY,
    val revision: Int = 0,
    val isStyled: Boolean = true,
    val needsLeadingSpace: Boolean = false,
) {

    /** Переводить некуда: язык раскладки переводчику неизвестен или совпадает с целевым. */
    val canTranslate: Boolean get() = source != null && source.code != target.code

    val request: TranslationRequest?
        get() = source?.takeIf { canTranslate }?.let {
            TranslationRequest(editor.text, it.code, target.code, revision)
        }

    val isTranslating: Boolean
        get() = request != translatedFor && editor.text.isNotBlank() && status.allowsTranslation

    /** Языки, без моделей которых пара не переведётся. */
    val pairCodes: List<String> get() = listOfNotNull(source?.code, target.code)

    /** Раскладка целевого языка; null — на нём не напечатать, и менять пару местами нельзя. */
    val swapLayout: KeyboardLanguage? get() = layouts[target.code]?.takeIf { source != null }
}

@Immutable
internal data class TranslationRequest(
    val text: String,
    val sourceCode: String,
    val targetCode: String,
    val revision: Int,
)

internal enum class TranslatorStatus(val allowsTranslation: Boolean = true) {
    READY,
    /** Модели пары нет на телефоне — предлагаем скачать. */
    MODEL_MISSING(allowsTranslation = false),
    DOWNLOADING(allowsTranslation = false),
    DOWNLOAD_FAILED(allowsTranslation = false),
    FAILED,
}
