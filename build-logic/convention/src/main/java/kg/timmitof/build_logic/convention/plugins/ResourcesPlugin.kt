package kg.timmitof.build_logic.convention.plugins

import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import kg.timmitof.build_logic.convention.resources.MokoAndroidResources
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType

/**
 * Общие ресурсы на moko-resources: `src/commonMain/moko-resources/<локаль>/strings.xml`, `base` — русский.
 * Android-вывод moko доводит [MokoAndroidResources].
 */
class ResourcesPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("keyboardfonts.kmp.library")
        pluginManager.apply("dev.icerock.mobile.multiplatform-resources")

        tasks.withType<GenerateMultiplatformResourcesTask>().configureEach {
            if (name != ANDROID_MAIN_TASK) return@configureEach

            val sourceDir = layout.projectDirectory.dir("src/commonMain/moko-resources").asFile
            val outputDir = outputResourcesDir
            doLast {
                MokoAndroidResources.writeScriptLocales(sourceDir, outputDir.get().asFile)
                MokoAndroidResources.markUntranslatable(sourceDir, outputDir.get().asFile)
            }
        }
    }

    private companion object {
        const val ANDROID_MAIN_TASK = "generateMRandroidMain"
    }
}
