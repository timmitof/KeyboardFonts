package kg.timmitof.keyboard.translate.data.repository

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kg.timmitof.keyboard.translate.data.download.DefaultTranslationLanguages
import kg.timmitof.keyboard.translate.data.download.TranslationModelWorker
import kg.timmitof.keyboard.translate.data.source.TranslatorPreferencesSource
import kg.timmitof.keyboard.translate.domain.model.TranslationModels
import kg.timmitof.keyboard.translate.domain.repository.TranslationDownloadRepository
import kg.timmitof.keyboard.translate.domain.repository.TranslationModelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Загрузки через WorkManager. Каждая модель в задаче помечена тегом с её кодом —
 * по тегам незавершённых задач видно, какие языки сейчас в очереди.
 */
@Singleton
class TranslationDownloadRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val models: TranslationModelRepository,
    private val defaults: DefaultTranslationLanguages,
    private val preferences: TranslatorPreferencesSource,
) : TranslationDownloadRepository {

    private val workManager: WorkManager by lazy { WorkManager.getInstance(context) }

    override fun observeModels(): Flow<TranslationModels> =
        combine(models.observeDownloaded(), workManager.getWorkInfosByTagFlow(TAG)) { downloaded, works ->
            val queued = works.filterNot { it.state.isFinished }.flatMapTo(mutableSetOf()) { it.queuedCodes() }
            TranslationModels(downloaded = downloaded, downloading = queued - downloaded)
        }.distinctUntilChanged()

    override fun defaultCodes(): List<String> = defaults.codes()

    override suspend fun scheduleDefaults() {
        val missing = defaultCodes() - models.downloadedCodes() - preferences.declinedModels()
        if (missing.isEmpty()) return

        // KEEP: ждущая Wi-Fi загрузка остаётся одна, сколько бы раз приложение ни запускалось.
        enqueue(DEFAULTS_WORK, missing, NetworkType.UNMETERED, ExistingWorkPolicy.KEEP)
    }

    override fun cancelDefaults() {
        workManager.cancelUniqueWork(DEFAULTS_WORK)
    }

    override suspend fun download(codes: Collection<String>) {
        codes.forEach { preferences.setDeclined(it, isDeclined = false) }
        val sorted = codes.toSortedSet()
        enqueue("$USER_WORK_PREFIX${sorted.joinToString("_")}", sorted, NetworkType.CONNECTED, ExistingWorkPolicy.KEEP)
    }

    override suspend fun delete(code: String): Boolean {
        if (models.isBuiltIn(code)) return false

        // Ждущая Wi-Fi загрузка по умолчанию могла бы скачать язык обратно: пересоздаём её
        // уже с учётом отметки «удалён пользователем».
        preferences.setDeclined(code, isDeclined = true)
        cancelDefaults()
        val isDeleted = models.delete(code)
        scheduleDefaults()
        return isDeleted
    }

    private fun enqueue(name: String, codes: Collection<String>, network: NetworkType, policy: ExistingWorkPolicy) {
        val request = OneTimeWorkRequestBuilder<TranslationModelWorker>()
            .setInputData(TranslationModelWorker.inputOf(codes))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(network).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
            .addTag(TAG)
            .apply { codes.forEach { addTag(CODE_TAG_PREFIX + it) } }
            .build()
        workManager.enqueueUniqueWork(name, policy, request)
    }

    private companion object {
        const val TAG = "translation_models"
        const val DEFAULTS_WORK = "translation_models_default"
        const val USER_WORK_PREFIX = "translation_models_user_"
        const val BACKOFF_SECONDS = 30L
    }
}

private const val CODE_TAG_PREFIX = "translation_model:"

private fun WorkInfo.queuedCodes(): List<String> =
    tags.filter { it.startsWith(CODE_TAG_PREFIX) }.map { it.removePrefix(CODE_TAG_PREFIX) }
