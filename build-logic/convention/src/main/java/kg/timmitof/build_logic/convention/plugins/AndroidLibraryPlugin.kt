package kg.timmitof.build_logic.convention.plugins

import com.android.build.api.dsl.LibraryExtension
import kg.timmitof.build_logic.convention.ProjectConfig
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * # `AndroidLibraryPlugin`
 *
 * `AndroidLibraryPlugin` is a Gradle plugin for Android projects that centralizes and simplifies the configuration of library modules.
 *
 * ## Purpose
 *
 * * **Simplified configuration**: a single point for core library project settings.
 * * **Flexibility**: easy to extend and add new common settings and tasks.
 * * **Consistency**: ensures all library modules follow the same configuration standards.
 * * **Maintainability**: reduces boilerplate and makes updates easier across multiple library modules.
 *
 * The plugin serves as a platform for further expansion and automation of tasks related to Android library modules, keeping information accurate and up-to-date as it evolves.
 *
 * ## Usage
 *
 * Apply the plugin in your library module's `build.gradle.kts`:
 *
 * ```
 * plugins {
 *     alias(libs.plugins.keyboardfonts.library)
 * }
 * ```
 */
class AndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        applyPlugins(target)
        applyProjectConfig(target)
    }

    private fun applyPlugins(project: Project) {
        project.apply {
            plugin("com.android.library")
            plugin("org.jetbrains.kotlin.android")
        }
    }

    private fun applyProjectConfig(project: Project) {
        project.application().apply {
            compileSdk = ProjectConfig.COMPILE_SDK

            defaultConfig {
                minSdk = ProjectConfig.MIN_SDK
            }

            compileOptions {
                sourceCompatibility = ProjectConfig.JAVA_VERSION
                targetCompatibility = ProjectConfig.JAVA_VERSION
            }
        }

        project.androidProject().apply {
            compilerOptions {
                jvmTarget.set(ProjectConfig.JVM_TARGET)
            }
        }
    }

    private fun Project.application(): LibraryExtension =
        extensions.getByType(LibraryExtension::class.java)

    private fun Project.androidProject(): KotlinAndroidProjectExtension =
        extensions.getByType(KotlinAndroidProjectExtension::class.java)
}