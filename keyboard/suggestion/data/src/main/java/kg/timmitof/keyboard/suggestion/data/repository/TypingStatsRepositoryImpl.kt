package kg.timmitof.keyboard.suggestion.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kg.timmitof.keyboard.suggestion.domain.model.TypingEvent
import kg.timmitof.keyboard.suggestion.domain.repository.TypingStatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * В памяти копятся только приросты с прошлого сохранения: файл читается в [flush], и счётчики не надо
 * загружать до первого события. Посмотреть сводку: `adb logcat -s TypingStats`, сбросить — удалить файл.
 */
@Singleton
class TypingStatsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : TypingStatsRepository {

    private val isEnabled: Boolean by lazy {
        context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    private val pending = ConcurrentHashMap<String, AtomicInteger>()

    private val mutex = Mutex()

    private val file by lazy { File(File(context.filesDir, DIRECTORY).apply { mkdirs() }, FILE_NAME) }

    override fun record(event: TypingEvent) {
        if (!isEnabled) return

        pending.getOrPut(event.key) { AtomicInteger() }.incrementAndGet()
    }

    override suspend fun flush() {
        if (!isEnabled || pending.isEmpty()) return

        withContext(Dispatchers.IO + NonCancellable) {
            mutex.withLock {
                val totals = read()
                pending.keys.toList().forEach { key ->
                    val delta = pending.remove(key)?.get() ?: return@forEach
                    totals[key] = (totals[key] ?: 0) + delta
                }
                runCatching { file.writeText(totals.entries.joinToString("\n") { "${it.key} ${it.value}" }) }
                Log.i(TAG, summary(totals))
            }
        }
    }

    private fun read(): MutableMap<String, Int> {
        val totals = LinkedHashMap<String, Int>()
        if (!file.exists()) return totals

        runCatching { file.readLines() }.getOrDefault(emptyList()).forEach { line ->
            val key = line.substringBefore(' ')
            val count = line.substringAfter(' ').toIntOrNull() ?: return@forEach
            totals[key] = count
        }
        return totals
    }

    private fun summary(totals: Map<String, Int>): String {
        fun count(key: String) = totals[key] ?: 0

        val kept = count(KEPT)
        val corrected = count(CORRECTED)
        val words = kept + corrected
        val picks = totals.keys
            .filter { it.startsWith(PICKED_PREFIX) }
            .sortedBy { it.removePrefix(PICKED_PREFIX).toIntOrNull() ?: Int.MAX_VALUE }
            .joinToString(" ") { "#${it.removePrefix(PICKED_PREFIX)}=${count(it)}" }

        return buildString {
            append("слов=").append(words)
            append(" автозамен=").append(corrected).append(" (").append(percent(corrected, words)).append(')')
            append(" откатов=").append(count(UNDONE)).append(" (").append(percent(count(UNDONE), corrected)).append(" от автозамен)")
            append(" стёрто после пробела=").append(count(ERASED)).append(" (").append(percent(count(ERASED), kept)).append(" от оставленных)")
            append(" подсказки: ").append(picks.ifEmpty { "—" })
            append(" предсказания=").append(count(PREDICTED))
            append(" умные зоны: подмен=").append(count(ADAPTED))
            append(" стёрто=").append(count(ADAPTED_UNDONE)).append(" (").append(percent(count(ADAPTED_UNDONE), count(ADAPTED))).append(')')
        }
    }

    private fun percent(part: Int, whole: Int): String =
        if (whole == 0) "—" else "${(part * 100f / whole).roundToInt()}%"

    private val TypingEvent.key: String
        get() = when (this) {
            TypingEvent.WordKept -> KEPT
            TypingEvent.AutoCorrected -> CORRECTED
            TypingEvent.AutoCorrectionUndone -> UNDONE
            TypingEvent.KeptWordErased -> ERASED
            TypingEvent.PredictionPicked -> PREDICTED
            TypingEvent.KeyAdapted -> ADAPTED
            TypingEvent.KeyAdaptationUndone -> ADAPTED_UNDONE
            is TypingEvent.SuggestionPicked -> "$PICKED_PREFIX$position"
        }

    private companion object {
        const val TAG = "TypingStats"
        const val DIRECTORY = "suggestions"
        const val FILE_NAME = "typing.stats"

        const val KEPT = "kept"
        const val CORRECTED = "corrected"
        const val UNDONE = "undone"
        const val ERASED = "erased"
        const val PREDICTED = "predicted"
        const val ADAPTED = "adapted"
        const val ADAPTED_UNDONE = "adapted_undone"
        const val PICKED_PREFIX = "picked_"
    }
}
