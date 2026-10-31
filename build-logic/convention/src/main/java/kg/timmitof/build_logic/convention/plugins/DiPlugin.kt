package kg.timmitof.build_logic.convention.plugins

import com.android.build.api.dsl.LibraryExtension
import kg.timmitof.build_logic.convention.featureModulePath
import kg.timmitof.build_logic.convention.featureNamespacePrefix
import kg.timmitof.build_logic.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** Koin-модули слоя в общем коде: граф собирают и приложение Android, и iOS. */
class DiPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("keyboardfonts.kmp.library")

        extensions.configure<LibraryExtension> {
            namespace = "${featureNamespacePrefix()}.di"
        }

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.commonMain.dependencies {
                implementation(libs.findLibrary("koin-core").get())

                // Слои подключаются, только если они есть: фича может не иметь своего data.
                featureModuleIfExists("domain")?.let { implementation(project(it)) }
                featureModuleIfExists("data")?.let { implementation(project(it)) }
            }
        }
    }

    private fun Project.featureModuleIfExists(name: String): String? {
        val path = featureModulePath(name)
        return if (findProject(path) != null) path else null
    }
}
