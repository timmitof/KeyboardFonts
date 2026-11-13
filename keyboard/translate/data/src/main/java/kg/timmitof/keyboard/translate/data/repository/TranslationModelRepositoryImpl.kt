package kg.timmitof.keyboard.translate.data.repository

import com.google.mlkit.nl.translate.TranslateLanguage
import kg.timmitof.keyboard.translate.data.runCatchingCancellable
import kg.timmitof.keyboard.translate.data.source.MlKitModelSource
import kg.timmitof.keyboard.translate.domain.model.TranslationLanguage
import kg.timmitof.keyboard.translate.domain.repository.TranslationModelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Список скачанных моделей держим в памяти: перевод спрашивает его на каждую паузу в наборе,
 * а запрос к ML Kit идёт через диск. Обновляем только после своих скачиваний и удалений.
 */
@Singleton
class TranslationModelRepositoryImpl @Inject constructor(
    private val source: MlKitModelSource,
) : TranslationModelRepository {

    /** Список ML Kit не меняется, а названия языков считать на каждое открытие незачем. */
    private val languages: List<TranslationLanguage> by lazy {
        TranslateLanguage.getAllLanguages()
            .map { code -> TranslationLanguage(code = code, name = nativeName(code)) }
            .sortedBy { it.name.lowercase() }
    }

    private val byCode: Map<String, TranslationLanguage> by lazy { languages.associateBy { it.code } }

    /** null — у ML Kit ещё не спрашивали. */
    private val downloaded = MutableStateFlow<Set<String>?>(null)

    private val loading = Mutex()

    override fun languages(): List<TranslationLanguage> = languages

    override fun language(code: String): TranslationLanguage? = byCode[code]

    override fun languageForLayout(layoutCode: String): TranslationLanguage? =
        TranslateLanguage.fromLanguageTag(layoutCode.substringBefore('_'))?.let(byCode::get)

    override fun isBuiltIn(code: String): Boolean = code in BUILT_IN

    override fun observeDownloaded(): Flow<Set<String>> = downloaded
        .onStart { downloadedCodes() }
        .filterNotNull()

    override suspend fun downloadedCodes(): Set<String> =
        downloaded.value ?: loading.withLock { downloaded.value ?: refresh() }

    override suspend fun download(codes: Collection<String>): Boolean {
        val missing = codes.toSet() - downloadedCodes()
        if (missing.isEmpty()) return true

        // Каждую модель пробуем скачать, даже если предыдущая не скачалась: так меньше повторных загрузок.
        val failed = missing.count { code -> runCatchingCancellable { source.download(code) } == null }
        refresh()
        return failed == 0
    }

    override suspend fun delete(code: String): Boolean {
        if (isBuiltIn(code)) return false

        val isDeleted = runCatchingCancellable { source.delete(code) } != null
        refresh()
        return isDeleted
    }

    /** Ошибка чтения — не повод считать, что моделей нет совсем: встроенные доступны всегда. */
    private suspend fun refresh(): Set<String> {
        val codes = (runCatchingCancellable { source.downloadedCodes() }.orEmpty()) + BUILT_IN
        downloaded.value = codes
        return codes
    }

    /** Название на самом языке с заглавной: «English», «Deutsch», «Русский». */
    private fun nativeName(code: String): String {
        val locale = Locale.forLanguageTag(code)
        return locale.getDisplayLanguage(locale).replaceFirstChar { it.titlecase(locale) }
    }

    private companion object {
        /** Английский встроен в ML Kit: его не качают и не удаляют. */
        val BUILT_IN = setOf(TranslateLanguage.ENGLISH)
    }
}
