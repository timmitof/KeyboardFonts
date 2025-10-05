package kg.timmitof.build_logic.convention.plugins

import com.android.build.gradle.LibraryExtension
import kg.timmitof.build_logic.convention.featureModulePath
import kg.timmitof.build_logic.convention.featureNamespacePrefix
import kg.timmitof.build_logic.convention.implementation
import kg.timmitof.build_logic.convention.ksp
import kg.timmitof.build_logic.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class DiPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("keyboardfonts.library")
        pluginManager.apply("org.jetbrains.kotlin.android")
        pluginManager.apply("dagger.hilt.android.plugin")
        pluginManager.apply("com.google.devtools.ksp")

        extensions.configure<LibraryExtension> {
            namespace = "${featureNamespacePrefix()}.di"
        }

        dependencies {
            implementation(libs.findLibrary("hilt-android").get())
            ksp(libs.findLibrary("hilt-compiler").get())

            implementation(project(featureModulePath("domain")))
            implementation(project(featureModulePath("data")))
            implementation(project(":core:common"))
        }
    }
}