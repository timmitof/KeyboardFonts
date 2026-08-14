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
 * Хранит выученные слова и пары слов в личных файлах приложения.
 *
 * Файл на язык, запись — во временный файл с последующим переименованием:
 * клавиатуру могут убить в любой момент, и недописанный словарь недопустим.
 */
@Singleton
class UserDictionaryStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private val directory by lazy { File(context.filesDir, DIRECTORY).apply { mkdirs() } }

    private val mutex = Mutex()

    internal suspend fun load(languageCode: String): UserLanguageModel = withContext(Dispatchers.IO) {
        val model = UserLanguageModel()
        val file = fileOf(languageCode)

        if (file.exists()) {
            runCatching { file.readLines() }
                .getOrDefault(emptyList())
                .let { model.restore(it.asSequence()) }
        }
        model
    }

    internal suspend fun save(languageCode: String, model: UserLanguageModel) = withContext(Dispatchers.IO) {
        val lines = model.export()

        mutex.withLock {
            runCatching {
                val temporary = File(directory, "$languageCode$MODEL_EXTENSION$TEMPORARY_SUFFIX")
                temporary.writeText(lines.joinToString("\n"))

                val target = fileOf(languageCode)
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
    }
}
