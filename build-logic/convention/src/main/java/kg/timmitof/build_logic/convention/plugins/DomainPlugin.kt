package kg.timmitof.build_logic.convention.plugins

import com.android.build.gradle.LibraryExtension
import kg.timmitof.build_logic.convention.featureNamespacePrefix
import kg.timmitof.build_logic.convention.implementation
import kg.timmitof.build_logic.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class DomainPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("keyboardfonts.library")

        extensions.configure<LibraryExtension> {
            namespace = "${featureNamespacePrefix()}.domain"
        }

        dependencies {
            implementation(project(":core:common"))
            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
        }
    }
}
