package kg.timmitof.build_logic.convention.plugins

import com.android.build.api.dsl.ApplicationExtension
import kg.timmitof.build_logic.convention.ProjectConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * # `AndroidApplicationPlugin`
 *
 * `AndroidApplicationPlugin` is a Gradle plugin for Android projects that centralizes and simplifies the configuration of application modules.
 *
 * ## Purpose
 *
 * * **Simplified configuration**: a single point for core project settings.
 * * **Flexibility**: easy to extend and add new common settings and tasks.
 *
 * The plugin serves as a platform for further expansion and automation of tasks related to Android applications, keeping information accurate and up-to-date as it evolves.
 *
 * ## Usage
 *
 * Apply the plugin in your module's `build.gradle.kts`:
 *
 * `plugins {
 *      alias(libs.plugins.keyboardfonts.application)
 *  }`
*/
class AndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        applyPlugins(target)
        applyProjectConfig(target)
    }

    private fun applyPlugins(project: Project) {
        project.apply {
            plugin("com.android.application")
            plugin("org.jetbrains.kotlin.android")
            plugin("org.jetbrains.kotlin.plugin.compose")
        }
    }

    private fun applyProjectConfig(project: Project) {
        project.application().apply {
            namespace = "kg.timmitof.keyboardfonts"

            compileSdk = ProjectConfig.COMPILE_SDK

            defaultConfig {
                applicationId = "kg.timmitof.keyboardfonts"

                minSdk = ProjectConfig.MIN_SDK
                targetSdk = ProjectConfig.TARGET_SDK
                versionCode = ProjectConfig.VERSION_CODE
                versionName = ProjectConfig.VERSION_NAME
            }

            compileOptions {
                sourceCompatibility = ProjectConfig.JAVA_VERSION
                targetCompatibility = ProjectConfig.JAVA_VERSION
            }

            buildFeatures {
                compose = true
                buildConfig = true
            }
        }

        project.androidProject().apply {
            compilerOptions {
                jvmTarget.set(ProjectConfig.JVM_TARGET)
            }
        }
    }

    private fun Project.application(): ApplicationExtension =
        extensions.getByType(ApplicationExtension::class.java)

    private fun Project.androidProject(): KotlinAndroidProjectExtension =
        extensions.getByType(KotlinAndroidProjectExtension::class.java)
}