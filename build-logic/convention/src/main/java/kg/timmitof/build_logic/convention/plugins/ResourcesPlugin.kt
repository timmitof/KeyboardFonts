package kg.timmitof.build_logic.convention.plugins

import dev.icerock.gradle.MultiplatformResourcesPluginExtension
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import kg.timmitof.build_logic.convention.resources.MokoAndroidResources
import kg.timmitof.build_logic.convention.resources.MokoAppleResources
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

/**
 * Общие ресурсы на moko-resources: `src/commonMain/moko-resources/<локаль>/strings.xml`, `base` — русский.
 * Вывод moko доводят [MokoAndroidResources] и [MokoAppleResources].
 */
class ResourcesPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("keyboardfonts.kmp.library")
        pluginManager.apply("dev.icerock.mobile.multiplatform-resources")

        extensions.configure<MultiplatformResourcesPluginExtension> {
            // Базовый язык (русский) лежит в Base.lproj: без перевода строка на iOS берётся оттуда, как из values/ на Android.
            iosBaseLocalizationRegion.set(IOS_BASE_LOCALIZATION)
        }
        // moko пакует бандл в doLast компиляции, а ни регион, ни сами строки не входят в её входы:
        // без этого правка строк или региона не пересоберёт бандл.
        val appleResources = tasks.withType<GenerateMultiplatformResourcesTask>()
            .matching { it.name.startsWith(IOS_TASK_PREFIX) }
        tasks.withType<KotlinNativeCompile>().configureEach {
            inputs.property("mokoIosBaseLocalizationRegion", IOS_BASE_LOCALIZATION)
            inputs.files(appleResources.map { it.outputResourcesDir })
                .withPropertyName("mokoGeneratedResources")
                .withPathSensitivity(PathSensitivity.RELATIVE)
        }

        tasks.withType<GenerateMultiplatformResourcesTask>().configureEach {
            if (name.startsWith(IOS_TASK_PREFIX) && name.endsWith(MAIN_TASK_SUFFIX)) {
                val outputDir = outputResourcesDir
                doLast { MokoAppleResources.fillMissingFromBase(outputDir.get().asFile) }
            }
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
        const val IOS_BASE_LOCALIZATION = "Base"
        const val IOS_TASK_PREFIX = "generateMRios"
        const val MAIN_TASK_SUFFIX = "Main"
    }
}
