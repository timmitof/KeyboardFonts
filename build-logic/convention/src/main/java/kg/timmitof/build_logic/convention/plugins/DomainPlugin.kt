package kg.timmitof.build_logic.convention.plugins

import kg.timmitof.build_logic.convention.implementation
import kg.timmitof.build_logic.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class DomainPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("java-library")
        pluginManager.apply("org.jetbrains.kotlin.jvm")

        dependencies {
            implementation(project(":core:common"))
            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
        }
    }
}
