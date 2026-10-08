package kg.timmitof.build_logic.convention.plugins

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import kg.timmitof.build_logic.convention.languages.GenerateLanguageCatalogTask
import kg.timmitof.build_logic.convention.languages.keyboardLanguages
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

/**
 * Собирает из каталога `keyboardLanguages` ассет `languages.json` — его читает клавиатура в рантайме —
 * и проверяет, что у каждого языка есть раскладка в `assets/layouts/`.
 */
class LanguageCatalogPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val catalog = keyboardLanguages()

        val generate = tasks.register<GenerateLanguageCatalogTask>("generateLanguageCatalog") {
            languages.set(catalog.languages)
            layouts.set(layout.projectDirectory.dir("src/main/assets/layouts"))
            outputDirectory.set(layout.buildDirectory.dir("generated/languageCatalog"))
        }

        pluginManager.withPlugin("com.android.library") {
            extensions.getByType<LibraryAndroidComponentsExtension>().onVariants { variant ->
                variant.sources.assets?.addGeneratedSourceDirectory(generate, GenerateLanguageCatalogTask::outputDirectory)
            }
        }
    }
}
