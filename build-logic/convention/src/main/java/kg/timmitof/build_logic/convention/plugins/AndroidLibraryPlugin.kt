package kg.timmitof.build_logic.convention.plugins

import com.android.build.api.dsl.LibraryExtension
import kg.timmitof.build_logic.convention.ProjectConfig
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

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