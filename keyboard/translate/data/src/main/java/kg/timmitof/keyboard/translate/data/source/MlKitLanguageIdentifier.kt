package kg.timmitof.keyboard.translate.data.source

import com.google.mlkit.nl.languageid.IdentifiedLanguage
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import com.google.mlkit.nl.languageid.LanguageIdentifier
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** ML Kit Language ID: модель встроена в библиотеку, сеть не нужна. Клиент создаётся при первом запросе. */
@Singleton
class MlKitLanguageIdentifier @Inject constructor() {

    private val lock = Any()

    private var client: LanguageIdentifier? = null

    /** Порог низкий: решать, верить ли, будет вызывающий — по всему списку, а не по одному ответу. */
    suspend fun identify(text: String): List<IdentifiedLanguage> =
        client().identifyPossibleLanguages(text).await()

    fun close() = synchronized(lock) {
        client?.close()
        client = null
    }

    private fun client(): LanguageIdentifier = synchronized(lock) {
        client ?: LanguageIdentification.getClient(
            LanguageIdentificationOptions.Builder()
                .setConfidenceThreshold(MIN_CONFIDENCE)
                .build()
        ).also { client = it }
    }

    private companion object {
        const val MIN_CONFIDENCE = 0.05f
    }
}
