package kg.timmitof.build_logic.convention.plugins

import com.android.build.gradle.LibraryExtension
import kg.timmitof.build_logic.convention.debugImplementation
import kg.timmitof.build_logic.convention.featureModulePath
import kg.timmitof.build_logic.convention.featureNamespacePrefix
import kg.timmitof.build_logic.convention.implementation
import kg.timmitof.build_logic.convention.ksp
import kg.timmitof.build_logic.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class PresentationPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("keyboardfonts.library")
        pluginManager.apply("org.jetbrains.kotlin.android")
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("dagger.hilt.android.plugin")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<LibraryExtension> {
            namespace = "${featureNamespacePrefix()}.presentation"
        }

        dependencies {
            implementation(libs.findLibrary("androidx-core-ktx").get())
            implementation(libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
            implementation(platform(libs.findLibrary("androidx-compose-bom").get()))
            implementation(libs.findLibrary("androidx-navigation-compose").get())
            implementation(libs.findLibrary("androidx-ui").get())
            implementation(libs.findLibrary("androidx-ui-tooling-preview").get())
            debugImplementation(libs.findLibrary("androidx-ui-tooling").get())
            implementation(libs.findLibrary("androidx-ui-graphics").get())
            implementation(libs.findLibrary("androidx-material3").get())
            implementation(libs.findLibrary("androidx-compose-material-icons").get())
            implementation(libs.findLibrary("coil-compose").get())
            implementation(libs.findLibrary("orbit-core").get())
            implementation(libs.findLibrary("orbit-viewmodel").get())
            implementation(libs.findLibrary("orbit-compose").get())

            implementation(libs.findLibrary("hilt-android").get())
            ksp(libs.findLibrary("hilt-compiler").get())
            implementation(libs.findLibrary("hilt-navigation").get())

            implementation(project(":core:ui"))
            implementation(project(":core:navigation"))
            featureModuleIfExists("domain")?.let { implementation(project(it)) }
        }
    }

    private fun Project.featureModuleIfExists(name: String): String? {
        val path = featureModulePath(name)
        return if (findProject(path) != null) path else null
    }
}