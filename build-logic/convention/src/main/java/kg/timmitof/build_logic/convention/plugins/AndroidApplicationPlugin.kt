package kg.timmitof.build_logic.convention.plugins

import com.android.build.api.dsl.ApplicationExtension
import kg.timmitof.build_logic.convention.ProjectConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

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

            buildTypes {
                debug {
                    isDebuggable = false
                }
            }

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