package kg.timmitof.build_logic.convention

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.getByType

fun DependencyHandler.implementation(dependencyNotation: Any): Dependency? =
    add("implementation", dependencyNotation)

fun DependencyHandler.ksp(dependencyNotation: Any): Dependency? =
    add("ksp", dependencyNotation)

fun DependencyHandler.testImplementation(dependencyNotation: Any): Dependency? =
    add("testImplementation", dependencyNotation)

fun DependencyHandler.androidTestImplementation(dependencyNotation: Any): Dependency? =
    add("androidTestImplementation", dependencyNotation)

fun DependencyHandler.debugImplementation(dependencyNotation: Any): Dependency? =
    add("debugImplementation", dependencyNotation)

fun Project.featureNamespacePrefix(): String {
    val featureName = project.path.split(":")
        .getOrNull(1) // e.g., feature_payment_data
        ?.removePrefix("feature_") // => payment_data
        ?.substringBeforeLast("_") // => payment
        ?: "feature"

    return "${project.group}.$featureName"
}

fun Project.featureModulePath(moduleName: String): String {
    val parts = path.split(":").filter { it.isNotBlank() }
    val rootModule = parts.getOrNull(0) ?: error("Invalid project path: $path")

    return if (rootModule.contains("feature")) ":$rootModule:${rootModule}_$moduleName"
        else ":$rootModule:$moduleName"
}

val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")