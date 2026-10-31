package kg.timmitof.build_logic.convention.plugins

import com.android.build.api.dsl.LibraryExtension
import kg.timmitof.build_logic.convention.featureModulePath
import kg.timmitof.build_logic.convention.featureNamespacePrefix
import kg.timmitof.build_logic.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** Data — общий код в `commonMain`, работа с платформой (файлы, буфер обмена, шрифты) — в `androidMain`/`iosMain`. */
class DataPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("keyboardfonts.kmp.library")

        extensions.configure<LibraryExtension> {
            namespace = "${featureNamespacePrefix()}.data"
        }

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.commonMain.dependencies {
                implementation(libs.findLibrary("kotlinx-coroutines-core").get())

                implementation(project(featureModulePath("domain")))
            }
        }
    }
}
