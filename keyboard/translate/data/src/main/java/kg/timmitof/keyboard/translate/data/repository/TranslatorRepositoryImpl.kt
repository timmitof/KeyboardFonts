package kg.timmitof.keyboard.translate.data.repository

import com.google.mlkit.nl.translate.TranslateLanguage
import kg.timmitof.keyboard.translate.data.MlKitTranslatorSource
import kg.timmitof.keyboard.translate.data.TranslatorPreferencesSource
import kg.timmitof.keyboard.translate.domain.model.TranslationLanguage
import kg.timmitof.keyboard.translate.domain.model.TranslationResult
import kg.timmitof.keyboard.translate.domain.model.TranslatorPreferences
import kg.timmitof.keyboard.translate.domain.repository.TranslatorRepository
import kotlinx.coroutines.CancellationException
import java.util.Locale
import javax.inject.Inject

class TranslatorRepositoryImpl @Inject constructor(
    private val translator: MlKitTranslatorSource,
    private val preferences: TranslatorPreferencesSource,
) : TranslatorRepository {

    /** Список ML Kit не меняется, а названия языков считать на каждое открытие незачем. */
    private val languages: List<TranslationLanguage> by lazy {
        TranslateLanguage.getAllLanguages()
            .map { code -> TranslationLanguage(code = code, name = nativeName(code)) }
            .sortedBy { it.name.lowercase() }
    }

    private val byCode: Map<String, TranslationLanguage> by lazy { languages.associateBy { it.code } }

    override fun languages(): List<TranslationLanguage> = languages

    override fun languageForLayout(layoutCode: String): TranslationLanguage? =
        TranslateLanguage.fromLanguageTag(layoutCode.substringBefore('_'))?.let(byCode::get)

    override fun language(code: String): TranslationLanguage? = byCode[code]

    override suspend fun downloadedCodes(): Set<String> =
        runCatchingCancellable { translator.downloadedCodes() } ?: emptySet()

    override suspend fun download(sourceCode: String, targetCode: String): Boolean =
        runCatchingCancellable { translator.download(sourceCode, targetCode) } != null

    override suspend fun translate(text: String, sourceCode: String, targetCode: String): TranslationResult {
        if (text.isBlank()) return TranslationResult.Success(text)

        val downloaded = downloadedCodes()
        if (sourceCode !in downloaded || targetCode !in downloaded) return TranslationResult.ModelMissing

        return runCatchingCancellable { translator.translate(text, sourceCode, targetCode) }
            ?.let { TranslationResult.Success(it.withEdgeSpaces(text)) }
            ?: TranslationResult.Failed
    }

    override suspend fun getPreferences(): TranslatorPreferences = preferences.get()

    override suspend fun setTarget(code: String) = preferences.setTarget(code)

    override suspend fun setStyled(isStyled: Boolean) = preferences.setStyled(isStyled)

    /** Название на самом языке с заглавной: «English», «Deutsch», «Русский». */
    private fun nativeName(code: String): String {
        val locale = Locale.forLanguageTag(code)
        return locale.getDisplayLanguage(locale).replaceFirstChar { it.titlecase(locale) }
    }

    /** Переводчик съедает пробел в конце, а без него следующее слово в поле приклеится к переводу. */
    private fun String.withEdgeSpaces(source: String): String =
        if (source.last().isWhitespace() && !lastOrNull().isWhitespaceOrNull()) "$this " else this

    private fun Char?.isWhitespaceOrNull(): Boolean = this == null || isWhitespace()

    /** Отмену корутины не глотаем: иначе устаревший перевод доедет до поля. */
    private inline fun <T> runCatchingCancellable(block: () -> T): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
}
