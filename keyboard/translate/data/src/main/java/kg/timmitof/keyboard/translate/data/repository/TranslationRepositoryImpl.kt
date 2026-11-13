package kg.timmitof.keyboard.translate.data.repository

import kg.timmitof.keyboard.translate.data.runCatchingCancellable
import kg.timmitof.keyboard.translate.data.source.TranslatorEngine
import kg.timmitof.keyboard.translate.data.text.SentenceSplitter
import kg.timmitof.keyboard.translate.data.text.TranslationMemory
import kg.timmitof.keyboard.translate.domain.model.TranslationResult
import kg.timmitof.keyboard.translate.domain.repository.TranslationModelRepository
import kg.timmitof.keyboard.translate.domain.repository.TranslationRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Переводит по предложениям: готовые лежат в [TranslationMemory], поэтому при наборе
 * движок переводит только последнее, меняющееся предложение.
 */
@Singleton
class TranslationRepositoryImpl @Inject constructor(
    private val engine: TranslatorEngine,
    private val models: TranslationModelRepository,
) : TranslationRepository {

    private val memory = TranslationMemory(MEMORY_CAPACITY)

    override suspend fun translate(text: String, sourceCode: String, targetCode: String): TranslationResult {
        if (text.isBlank()) return TranslationResult.Success(text)

        val downloaded = models.downloadedCodes()
        if (sourceCode !in downloaded || targetCode !in downloaded) return TranslationResult.ModelMissing

        val translated = runCatchingCancellable {
            buildString {
                for (piece in SentenceSplitter.split(text)) append(translatePiece(piece, sourceCode, targetCode))
            }
        }
        return translated?.let(TranslationResult::Success) ?: TranslationResult.Failed
    }

    override fun release() = engine.close()

    /** Пробелы и переводы строк вокруг предложения движок съедает — возвращаем их на место. */
    private suspend fun translatePiece(piece: String, sourceCode: String, targetCode: String): String {
        val core = piece.trim()
        if (core.isEmpty()) return piece

        val leading = piece.takeWhile(Char::isWhitespace)
        val trailing = piece.takeLastWhile(Char::isWhitespace)
        val key = TranslationMemory.Key(sourceCode, targetCode, core)
        val translated = memory[key] ?: engine.translate(core, sourceCode, targetCode).also { memory[key] = it }
        return leading + translated + trailing
    }

    private companion object {
        /** Пара сотен предложений — несколько длинных переписок, в памяти это килобайты. */
        const val MEMORY_CAPACITY = 256
    }
}
