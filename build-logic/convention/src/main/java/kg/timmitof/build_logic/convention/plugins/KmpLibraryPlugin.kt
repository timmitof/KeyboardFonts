package kg.timmitof.build_logic.convention.plugins

import com.android.build.api.dsl.LibraryExtension
import kg.timmitof.build_logic.convention.ProjectConfig
import kg.timmitof.build_logic.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** Мультиплатформенная библиотека: Android + iOS (устройство и симулятор Apple Silicon). Код — в `src/commonMain/kotlin`. */
class KmpLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")

        extensions.configure<KotlinMultiplatformExtension> {
            androidTarget {
                compilerOptions {
                    jvmTarget.set(ProjectConfig.JVM_TARGET)
                }
            }
            iosArm64()
            iosSimulatorArm64()

            sourceSets.commonTest.dependencies {
                implementation(libs.findLibrary("kotlin-test").get())
            }
        }

        extensions.configure<LibraryExtension> {
            compileSdk = ProjectConfig.COMPILE_SDK

            defaultConfig {
                minSdk = ProjectConfig.MIN_SDK
            }

            compileOptions {
                sourceCompatibility = ProjectConfig.JAVA_VERSION
                targetCompatibility = ProjectConfig.JAVA_VERSION
            }
        }
    }
}
