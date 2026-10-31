package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.core.data.AppDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/** Файл на язык. Запись через временный файл и переименование: IME могут убить в любой момент. */
class UserDictionaryStore(
    private val appDirectory: AppDirectory,
) {

    private val fileSystem: FileSystem = systemFileSystem

    private val directory: Path by lazy {
        (appDirectory.path.toPath() / DIRECTORY).also(fileSystem::createDirectories)
    }

    private val mutex = Mutex()

    internal suspend fun load(languageCode: String): UserLanguageModel = withContext(Dispatchers.IO) {
        val model = UserLanguageModel()
        val file = fileOf(languageCode)

        if (fileSystem.exists(file)) {
            runCatching { fileSystem.read(file) { readUtf8() }.lines() }
                .getOrDefault(emptyList())
                .let { model.restore(it.asSequence()) }
        }
        model
    }

    internal suspend fun save(languageCode: String, model: UserLanguageModel) = withContext(Dispatchers.IO) {
        val lines = model.export()

        mutex.withLock {
            runCatching {
                val temporary = directory / "$languageCode$MODEL_EXTENSION$TEMPORARY_SUFFIX"
                fileSystem.write(temporary) { writeUtf8(lines.joinToString("\n")) }
                fileSystem.atomicMove(temporary, fileOf(languageCode))
            }
        }
        Unit
    }

    private fun fileOf(languageCode: String): Path = directory / "$languageCode$MODEL_EXTENSION"

    private companion object {
        const val DIRECTORY = "suggestions"
        const val MODEL_EXTENSION = ".model"
        const val TEMPORARY_SUFFIX = ".tmp"
    }
}
