package kg.timmitof.keyboard.translate.data.repository

import kg.timmitof.keyboard.translate.data.runCatchingCancellable
import kg.timmitof.keyboard.translate.data.source.MlKitLanguageIdentifier
import kg.timmitof.keyboard.translate.domain.model.DetectedLanguage
import kg.timmitof.keyboard.translate.domain.repository.LanguageDetectionRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Теги ML Kit приводим к базовому языку: `uz-Latn` и `uz` — один язык для раскладок и переводчика. */
@Singleton
class LanguageDetectionRepositoryImpl @Inject constructor(
    private val identifier: MlKitLanguageIdentifier,
) : LanguageDetectionRepository {

    override suspend fun detect(text: String): List<DetectedLanguage> {
        val identified = runCatchingCancellable { identifier.identify(text) }.orEmpty()

        return identified
            .filter { it.languageTag != UNDETERMINED }
            .groupBy { it.languageTag.substringBefore('-').lowercase() }
            .map { (code, variants) -> DetectedLanguage(code, variants.sumOf { it.confidence.toDouble() }.toFloat()) }
            .sortedByDescending { it.confidence }
    }

    override fun release() = identifier.close()

    private companion object {
        /** «Не понять» у ML Kit. */
        const val UNDETERMINED = "und"
    }
}
