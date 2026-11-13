package kg.timmitof.keyboard.translate.data

import kg.timmitof.keyboard.translate.data.repository.TranslationRepositoryImpl
import kg.timmitof.keyboard.translate.data.source.TranslatorEngine
import kg.timmitof.keyboard.translate.domain.model.TranslationLanguage
import kg.timmitof.keyboard.translate.domain.model.TranslationResult
import kg.timmitof.keyboard.translate.domain.repository.TranslationModelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class TranslationRepositoryImplTest {

    private val engine = FakeEngine()

    private val repository = TranslationRepositoryImpl(engine, FakeModels(downloaded = setOf("ru", "en")))

    @Test
    fun `готовые предложения берутся из памяти, движок переводит только новое`() = runBlocking {
        repository.translate("Привет. Как", "ru", "en")
        engine.calls.clear()

        val result = repository.translate("Привет. Как дела", "ru", "en")

        assertEquals(TranslationResult.Success("[Привет.] [Как дела]"), result)
        assertEquals(listOf("Как дела"), engine.calls)
    }

    @Test
    fun `пробелы вокруг предложений сохраняются`() = runBlocking {
        val result = repository.translate("Да!  Нет ", "ru", "en")

        assertEquals(TranslationResult.Success("[Да!]  [Нет] "), result)
    }

    @Test
    fun `без скачанной модели не переводит`() = runBlocking {
        val result = repository.translate("Hallo", "de", "en")

        assertEquals(TranslationResult.ModelMissing, result)
        assertEquals(emptyList<String>(), engine.calls)
    }

    @Test
    fun `ошибка движка — Failed`() = runBlocking {
        engine.fails = true

        assertEquals(TranslationResult.Failed, repository.translate("Привет", "ru", "en"))
    }

    private class FakeEngine : TranslatorEngine {
        val calls = mutableListOf<String>()
        var fails = false

        override suspend fun translate(text: String, sourceCode: String, targetCode: String): String {
            if (fails) error("ML Kit недоступен")
            calls += text
            return "[$text]"
        }

        override fun close() = Unit
    }

    private class FakeModels(private val downloaded: Set<String>) : TranslationModelRepository {
        override fun languages(): List<TranslationLanguage> = emptyList()
        override fun language(code: String): TranslationLanguage? = null
        override fun languageForLayout(layoutCode: String): TranslationLanguage? = null
        override fun isBuiltIn(code: String): Boolean = code == "en"
        override fun observeDownloaded(): Flow<Set<String>> = flowOf(downloaded)
        override suspend fun downloadedCodes(): Set<String> = downloaded
        override suspend fun download(codes: Collection<String>): Boolean = true
        override suspend fun delete(code: String): Boolean = true
    }
}
