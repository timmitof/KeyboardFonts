package kg.timmitof.keyboard.suggestion.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Файл на язык и общий файл калибровки касаний. Запись через временный файл и переименование:
 * IME могут убить в любой момент.
 */
@Singleton
class UserDictionaryStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private val directory by lazy { File(context.filesDir, DIRECTORY).apply { mkdirs() } }

    private val calibrationFile by lazy { File(directory, CALIBRATION_FILE) }

    private val mutex = Mutex()

    internal suspend fun load(languageCode: String): UserLanguageModel =
        read(fileOf(languageCode), UserLanguageModel()) { restore(it) }

    internal suspend fun save(languageCode: String, model: UserLanguageModel) =
        write(fileOf(languageCode), model.export())

    internal suspend fun loadCalibration(): TouchCalibration =
        read(calibrationFile, TouchCalibration()) { restore(it) }

    internal suspend fun saveCalibration(calibration: TouchCalibration) =
        write(calibrationFile, calibration.export())

    /** Разбор тоже в фоне: личный словарь — тысячи строк. */
    private suspend fun <T> read(file: File, target: T, restore: T.(Sequence<String>) -> Unit): T =
        withContext(Dispatchers.IO) {
            if (file.exists()) {
                runCatching { file.readLines() }
                    .getOrDefault(emptyList())
                    .let { target.restore(it.asSequence()) }
            }
            target
        }

    private suspend fun write(target: File, lines: List<String>) = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                val temporary = File(directory, "${target.name}$TEMPORARY_SUFFIX")
                temporary.writeText(lines.joinToString("\n"))

                if (!temporary.renameTo(target)) {
                    target.delete()
                    temporary.renameTo(target)
                }
            }
        }
        Unit
    }

    private fun fileOf(languageCode: String) = File(directory, "$languageCode$MODEL_EXTENSION")

    private companion object {
        const val DIRECTORY = "suggestions"
        const val MODEL_EXTENSION = ".model"
        const val TEMPORARY_SUFFIX = ".tmp"
        const val CALIBRATION_FILE = "touch.calibration"
    }
}
