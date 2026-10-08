package kg.timmitof.build_logic.convention.plugins

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import kg.timmitof.build_logic.convention.dictionaries.GenerateDictionariesTask
import kg.timmitof.build_logic.convention.languages.LanguageSpec
import kg.timmitof.build_logic.convention.languages.keyboardLanguages
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

/**
 * Собирает словари Т9 из частотных списков при сборке и кладёт их в ассеты как сгенерированные:
 * в репозитории — только исходный список, на телефоне — готовый отсортированный словарь без разбора.
 * Рядом со словарём — индекс опечаток `<code>.spell` и фильтр словоформ `<code>.forms`, если у языка задан список форм.
 *
 * Какие словари собирать, берётся из каталога `keyboardLanguages` (блок `dictionary(...)` языка);
 * исходники лежат в `dictionaries/` модуля.
 */
class DictionariesPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val specs = keyboardLanguages().languages.map { languages -> languages.mapNotNull(LanguageSpec::dictionary) }
        val sourceDirectory = layout.projectDirectory.dir(SOURCE_DIRECTORY)

        val generate = tasks.register<GenerateDictionariesTask>("generateDictionaries") {
            languages.set(specs)
            sources.from(
                specs.map { list ->
                    list.flatMap { spec -> listOfNotNull(spec.source, spec.forms).map(sourceDirectory::file) }
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
