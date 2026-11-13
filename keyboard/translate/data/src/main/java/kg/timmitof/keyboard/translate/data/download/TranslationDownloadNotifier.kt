package kg.timmitof.keyboard.translate.data.download

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kg.timmitof.keyboard.translate.data.R
import kg.timmitof.keyboard.translate.domain.repository.TranslationModelRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Уведомление о загрузке языков. ML Kit не сообщает, сколько скачано, поэтому полоса бегущая,
 * а прогресс — по языкам («1 из 2»). Без разрешения на уведомления загрузка идёт молча.
 */
@Singleton
class TranslationDownloadNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val models: TranslationModelRepository,
) {

    private val manager = NotificationManagerCompat.from(context)

    fun showProgress(notificationId: Int, code: String, index: Int, total: Int) {
        val builder = baseBuilder()
            .setContentTitle(context.getString(R.string.translator_download_title))
            .setContentText(nameOf(code))
            .setProgress(0, 0, true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
        if (total > 1) builder.setSubText(context.getString(R.string.translator_download_step, index + 1, total))

        notify(notificationId, builder)
    }

    fun showReady(notificationId: Int, codes: List<String>) {
        val builder = baseBuilder()
            .setContentTitle(context.getString(R.string.translator_ready_title))
            .setContentText(context.getString(R.string.translator_ready_text, codes.joinToString(transform = ::nameOf)))
            .setAutoCancel(true)
            .setTimeoutAfter(READY_TIMEOUT_MILLIS)

        notify(notificationId, builder)
    }

    fun showFailed(notificationId: Int) {
        val builder = baseBuilder()
            .setContentTitle(context.getString(R.string.translator_failed_title))
            .setContentText(context.getString(R.string.translator_failed_text))
            .setAutoCancel(true)

        notify(notificationId, builder)
    }

    fun cancel(notificationId: Int) = manager.cancel(notificationId)

    private fun baseBuilder(): NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_translate)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentIntent(openAppIntent())

    /** Разрешение проверяет [NotificationManagerCompat.areNotificationsEnabled]: на Android 13+ без него — false. */
    @SuppressLint("MissingPermission")
    private fun notify(notificationId: Int, builder: NotificationCompat.Builder) {
        if (!manager.areNotificationsEnabled()) return

        ensureChannel()
        runCatching { manager.notify(notificationId, builder.build()) }
    }

    private fun ensureChannel() {
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(context.getString(R.string.translator_channel_name))
            .setDescription(context.getString(R.string.translator_channel_description))
            .build()
        manager.createNotificationChannel(channel)
    }

    private fun openAppIntent(): PendingIntent? {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun nameOf(code: String): String = models.language(code)?.name ?: code

    private companion object {
        const val CHANNEL_ID = "translator_downloads"
        const val READY_TIMEOUT_MILLIS = 6_000L
    }
}
