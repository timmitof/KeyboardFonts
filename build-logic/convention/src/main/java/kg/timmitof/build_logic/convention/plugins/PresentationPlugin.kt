package kg.timmitof.build_logic.convention.plugins

import com.android.build.api.dsl.LibraryExtension
import kg.timmitof.build_logic.convention.featureModulePath
import kg.timmitof.build_logic.convention.featureNamespacePrefix
import kg.timmitof.build_logic.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** Экраны фичи на Compose Multiplatform: общий код для Android и iOS, MVI на Orbit, DI на Koin. */
class PresentationPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("keyboardfonts.kmp.compose")

        extensions.configure<LibraryExtension> {
            namespace = "${featureNamespacePrefix()}.presentation"
        }

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.commonMain.dependencies {
                implementation(libs.findLibrary("jetbrains-compose-material-icons-core").get())
                implementation(libs.findLibrary("jetbrains-lifecycle-viewmodel-compose").get())
                implementation(libs.findLibrary("jetbrains-lifecycle-runtime-compose").get())
                implementation(libs.findLibrary("coil-compose").get())
                implementation(libs.findLibrary("orbit-core").get())
                implementation(libs.findLibrary("orbit-viewmodel").get())
                implementation(libs.findLibrary("orbit-compose").get())

                implementation(libs.findLibrary("koin-core").get())
                implementation(libs.findLibrary("koin-compose-viewmodel").get())

                implementation(project(":core:resources"))
                implementation(project(":core:ui"))
                implementation(project(":core:navigation"))
                featureModuleIfExists("domain")?.let { implementation(project(it)) }
            }
        }
    }

    private fun Project.featureModuleIfExists(name: String): String? {
        val path = featureModulePath(name)
        return if (findProject(path) != null) path else null
    }
}
