package kg.timmitof.build_logic.convention.plugins

import kg.timmitof.build_logic.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * UI на Compose Multiplatform: общий код экранов для Android и iOS.
 * Графика и шрифты — в `src/commonMain/composeResources` (векторы Android XML рисуются и на iOS), строки — в moko.
 */
class KmpComposePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("keyboardfonts.kmp.library")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        pluginManager.apply("org.jetbrains.compose")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.commonMain.dependencies {
                implementation(libs.findLibrary("jetbrains-compose-runtime").get())
                implementation(libs.findLibrary("jetbrains-compose-foundation").get())
                implementation(libs.findLibrary("jetbrains-compose-ui").get())
                implementation(libs.findLibrary("jetbrains-compose-material3").get())
                implementation(libs.findLibrary("jetbrains-compose-components-resources").get())
                implementation(libs.findLibrary("jetbrains-compose-ui-tooling-preview").get())
            }
        }

        dependencies {
            add("debugImplementation", libs.findLibrary("androidx-ui-tooling").get())
        }
    }
}
