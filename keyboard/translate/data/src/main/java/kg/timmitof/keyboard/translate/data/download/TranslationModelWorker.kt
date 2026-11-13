package kg.timmitof.keyboard.translate.data.download

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kg.timmitof.keyboard.translate.domain.repository.TranslationModelRepository

/**
 * Качает модели языков по одной и показывает уведомление. Не скачалось — WorkManager повторит
 * с паузой; после нескольких попыток сдаёмся до следующего запуска (или до кнопки «Скачать»).
 */
@HiltWorker
class TranslationModelWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val models: TranslationModelRepository,
    private val notifier: TranslationDownloadNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val codes = inputData.getStringArray(KEY_CODES).orEmpty().toList()
        val missing = codes - models.downloadedCodes()
        if (missing.isEmpty()) return Result.success()

        val notificationId = id.hashCode()
        val failed = missing.filterIndexed { index, code ->
            notifier.showProgress(notificationId, code, index, missing.size)
            !models.download(listOf(code))
        }

        return when {
            failed.isEmpty() -> {
                notifier.showReady(notificationId, missing)
                Result.success()
            }

            runAttemptCount + 1 < MAX_ATTEMPTS -> {
                notifier.cancel(notificationId)
                Result.retry()
            }

            else -> {
                notifier.showFailed(notificationId)
                Result.failure()
            }
        }
    }

    companion object {
        private const val KEY_CODES = "codes"
        private const val MAX_ATTEMPTS = 3

        fun inputOf(codes: Collection<String>) = workDataOf(KEY_CODES to codes.toTypedArray())
    }
}
