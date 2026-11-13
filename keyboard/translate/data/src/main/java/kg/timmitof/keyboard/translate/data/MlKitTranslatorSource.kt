package kg.timmitof.keyboard.translate.data

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Обёртка над ML Kit. Клиент держим один — на текущую пару: пару меняют редко,
 * а переводят на каждую паузу в наборе, и пересоздавать клиент каждый раз дорого.
 */
@Singleton
class MlKitTranslatorSource @Inject constructor() {

    private val modelManager: RemoteModelManager by lazy { RemoteModelManager.getInstance() }

    private val mutex = Mutex()

    private var client: PairClient? = null

    suspend fun downloadedCodes(): Set<String> =
        modelManager.getDownloadedModels(TranslateRemoteModel::class.java).await()
            .mapTo(mutableSetOf()) { it.language }

    suspend fun download(sourceCode: String, targetCode: String) {
        clientFor(sourceCode, targetCode)
            .downloadModelIfNeeded(DownloadConditions.Builder().build())
            .await()
    }

    suspend fun translate(text: String, sourceCode: String, targetCode: String): String =
        clientFor(sourceCode, targetCode).translate(text).await()

    private suspend fun clientFor(sourceCode: String, targetCode: String): Translator = mutex.withLock {
        client?.takeIf { it.source == sourceCode && it.target == targetCode }?.let { return it.translator }

        client?.translator?.close()
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceCode)
            .setTargetLanguage(targetCode)
            .build()
        Translation.getClient(options).also { client = PairClient(sourceCode, targetCode, it) }
    }

    private class PairClient(
        val source: String,
        val target: String,
        val translator: Translator,
    )
}
