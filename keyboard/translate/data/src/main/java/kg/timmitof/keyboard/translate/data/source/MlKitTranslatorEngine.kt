package kg.timmitof.keyboard.translate.data.source

import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Перевод через ML Kit. Клиент держим один — на текущую пару: пару меняют редко, а переводят
 * на каждую паузу в наборе, и загружать модель в память каждый раз дорого.
 */
@Singleton
class MlKitTranslatorEngine @Inject constructor() : TranslatorEngine {

    private val lock = Any()

    private var client: PairClient? = null

    override suspend fun translate(text: String, sourceCode: String, targetCode: String): String =
        clientFor(sourceCode, targetCode).translate(text).await()

    override fun close() = synchronized(lock) {
        client?.translator?.close()
        client = null
    }

    private fun clientFor(sourceCode: String, targetCode: String): Translator = synchronized(lock) {
        client?.takeIf { it.isFor(sourceCode, targetCode) }?.let { return it.translator }

        client?.translator?.close()
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceCode)
            .setTargetLanguage(targetCode)
            .build()
        Translation.getClient(options).also { client = PairClient(sourceCode, targetCode, it) }
    }

    private class PairClient(
        private val sourceCode: String,
        private val targetCode: String,
        val translator: Translator,
    ) {
        fun isFor(source: String, target: String): Boolean = sourceCode == source && targetCode == target
    }
}
