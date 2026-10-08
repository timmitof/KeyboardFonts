package kg.timmitof.build_logic.convention.plugins

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import kg.timmitof.build_logic.convention.dictionaries.DictionariesExtension
import kg.timmitof.build_logic.convention.dictionaries.GenerateDictionariesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

/**
 * Собирает словари Т9 из частотных списков при сборке и кладёт их в ассеты как сгенерированные:
 * в репозитории — только исходный список, на телефоне — готовый отсортированный словарь без разбора.
 * Рядом со словарём — индекс опечаток `<code>.spell` и фильтр словоформ `<code>.forms`, если у языка задан список форм.
 */
class DictionariesPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val extension = extensions.create<DictionariesExtension>("dictionaries").apply {
            sourceDirectory.convention(layout.projectDirectory.dir(SOURCE_DIRECTORY))
        }

        val generate = tasks.register<GenerateDictionariesTask>("generateDictionaries") {
            languages.set(extension.languages)
            sources.from(
                extension.sourceDirectory.zip(extension.languages) { directory, specs ->
                    specs.flatMap { spec -> listOfNotNull(spec.source, spec.forms).map(directory::file) }
                }
            )
            staticAssets.set(layout.projectDirectory.dir("src/main/assets"))
            outputDirectory.set(layout.buildDirectory.dir("generated/dictionaries"))
        }

        pluginManager.withPlugin("com.android.library") {
            extensions.getByType<LibraryAndroidComponentsExtension>().onVariants { variant ->
                variant.sources.assets?.addGeneratedSourceDirectory(generate, GenerateDictionariesTask::outputDirectory)
            }
        }
    }

    private companion object {
        const val SOURCE_DIRECTORY = "dictionaries"
    }
}
