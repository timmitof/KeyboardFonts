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
 * @param layouts включённые раскладки по коду языка перевода: из них выбирают исходный язык и с ними меняют пару местами.
 * @param models скачанные и стоящие в очереди модели: по ним панель знает, можно ли переводить.
 * @param translatedFor запрос, для которого готов [translation]; отличается от [request] — перевод в пути.
 * @param revision растёт, когда тот же текст нужно перевести заново (скачали язык, повторили после ошибки).
 * @param needsLeadingSpace в поле перед курсором уже есть текст без пробела — перевод отделяем пробелом.
 * @param isFontPickerOpen вместо пары языков — карусель шрифтов («Aa»): выбранный шрифт применяется к переводу.
 * @param recentTargets недавние языки перевода, свежие первыми — чипы над списком языков.
 * @param languageSearch запрос поиска языка перевода; null — поиск закрыт. Набирается обычными клавишами.
 */
@Immutable
internal data class TranslatorState(
    val source: TranslationLanguage?,
    val target: TranslationLanguage,
    val languages: List<TranslationLanguage>,
    val layouts: Map<String, KeyboardLanguage> = emptyMap(),
    val models: TranslationModels = TranslationModels(),
    val editor: InlineEditor = InlineEditor(),
    val translation: String = "",
    val translatedFor: TranslationRequest? = null,
    val status: TranslatorStatus = TranslatorStatus.READY,
    val revision: Int = 0,
    val needsLeadingSpace: Boolean = false,
    val isFontPickerOpen: Boolean = false,
    val recentTargets: List<TranslationLanguage> = emptyList(),
    val languageSearch: InlineEditor? = null,
) {

    /** Переводить некуда: язык раскладки переводчику неизвестен или совпадает с целевым. */
    val canTranslate: Boolean get() = source != null && source.code != target.code

    /** Языки, без моделей которых пара не переведётся. */
    val pairCodes: List<String> get() = listOfNotNull(source?.code, target.code)

    val isPairDownloaded: Boolean get() = pairCodes.all { it in models.downloaded }

    private val isPairDownloading: Boolean
        get() = status == TranslatorStatus.DOWNLOADING || pairCodes.any { it in models.downloading }

    /** Что показать вместо поля; null — поле с набранным текстом. Порядок — от главной причины к частной. */
    val notice: TranslatorNotice?
        get() = when {
            source == null -> TranslatorNotice.UNSUPPORTED_SOURCE
            !canTranslate -> TranslatorNotice.SAME_LANGUAGES
            isPairDownloaded -> if (status == TranslatorStatus.FAILED) TranslatorNotice.TRANSLATION_FAILED else null
            isPairDownloading -> TranslatorNotice.DOWNLOADING
            status == TranslatorStatus.DOWNLOAD_FAILED -> TranslatorNotice.DOWNLOAD_FAILED
            else -> TranslatorNotice.MODEL_MISSING
        }

    val request: TranslationRequest?
        get() = source?.takeIf { canTranslate }?.let {
            TranslationRequest(editor.text, it.code, target.code, revision)
        }

    /** Перевод в пути — тонкая полоска внизу поля. */
    val isTranslating: Boolean
        get() = request != translatedFor && editor.text.isNotBlank() && notice == null

    /** Раскладка целевого языка; null — на нём не напечатать, и менять пару местами нельзя. */
    val swapLayout: KeyboardLanguage? get() = layouts[target.code]?.takeIf { source != null }

    /** Найденные языки перевода: по названию и коду, скачанные — первыми. Пустой запрос — весь список. */
    val searchResults: List<TranslationLanguage>
        get() {
            val query = languageSearch?.text?.trim().orEmpty()
            return languages
                .filter { query.isEmpty() || it.name.contains(query, ignoreCase = true) || it.code.startsWith(query, ignoreCase = true) }
                .sortedByDescending { it.code in models.downloaded }
        }

    /** Языки, с которых можно переводить: включённые раскладки, которые знает переводчик. */
    val sourceLanguages: List<TranslationLanguage>
        get() = languages.filter { it.code in layouts }
}

@Immutable
internal data class TranslationRequest(
    val text: String,
    val sourceCode: String,
    val targetCode: String,
    val revision: Int,
)

/** Исход последнего действия; скачана ли пара, выводится из моделей — см. [TranslatorState.notice]. */
internal enum class TranslatorStatus { READY, DOWNLOADING, DOWNLOAD_FAILED, FAILED }

/** Карточка вместо поля (дизайн 8c): не ошибка, а что сделать дальше. */
internal enum class TranslatorNotice {
    UNSUPPORTED_SOURCE,
    SAME_LANGUAGES,
    MODEL_MISSING,
    DOWNLOADING,
    DOWNLOAD_FAILED,
    TRANSLATION_FAILED,
}
