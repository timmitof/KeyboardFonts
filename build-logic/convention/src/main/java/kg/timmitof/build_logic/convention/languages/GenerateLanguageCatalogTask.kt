package kg.timmitof.build_logic.convention.languages

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Каталог → `languages.json` в ассетах:
 *
 * ```
 * {"languages":[{"code":"ru_ru","name":"Русский","shortName":"RU","isLatin":false,
 *                "layout":"ru_ru","alphabet":"абв…","hasDictionary":true}, …]}
 * ```
 *
 * Порядок — порядок каталога. Язык без раскладки — ошибка сборки, а не падение клавиатуры на телефоне.
 */
@CacheableTask
abstract class GenerateLanguageCatalogTask : DefaultTask() {

    @get:Input
    abstract val languages: ListProperty<LanguageSpec>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val layouts: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val specs = languages.get()
        if (specs.isEmpty()) throw GradleException("keyboardLanguages: каталог пуст — нужен хотя бы один язык")

        val layoutsDirectory = layouts.get().asFile
        specs.forEach { spec ->
            if (!layoutsDirectory.resolve("${spec.layout}.json").isFile) {
                throw GradleException(
                    "keyboardLanguages: у языка ${spec.code} нет раскладки ${spec.layout}.json в $layoutsDirectory"
                )
            }
        }

        val output = outputDirectory.get().asFile
        output.deleteRecursively()
        output.mkdirs()

        val json = specs.joinToString(separator = ",\n", prefix = "{\"languages\":[\n", postfix = "\n]}\n") { spec ->
            buildString {
                append("  {")
                append("\"code\":").append(spec.code.quoted()).append(',')
                append("\"name\":").append(spec.name.quoted()).append(',')
                append("\"shortName\":").append(spec.shortName.quoted()).append(',')
                append("\"isLatin\":").append(spec.isLatin).append(',')
                append("\"layout\":").append(spec.layout.quoted()).append(',')
                append("\"alphabet\":").append(spec.alphabet.quoted()).append(',')
                append("\"hasDictionary\":").append(spec.dictionary != null)
                append('}')
            }
        }
        output.resolve(FILE_NAME).writeText(json, Charsets.UTF_8)
        logger.lifecycle("keyboardLanguages: в каталоге ${specs.size} языков — ${specs.joinToString { it.code }}")
    }

    private fun String.quoted(): String = buildString(length + 2) {
        append('"')
        this@quoted.forEach { char ->
            when {
                char == '"' -> append("\\\"")
                char == '\\' -> append("\\\\")
                char < ' ' -> append("\\u%04x".format(char.code))
                else -> append(char)
            }
        }
        append('"')
    }

    companion object {
        const val FILE_NAME = "languages.json"
    }
}
