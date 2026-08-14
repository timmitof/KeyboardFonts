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

/**
 * Префикс namespace модуля: [ProjectConfig.BASE_NAMESPACE] + родительские сегменты пути.
 *
 * Примеры:
 * - `:keyboard:presentation` → `kg.timmitof.keyboard`
 * - `:keyboard:suggestion:data` → `kg.timmitof.keyboard.suggestion`
 * - `:feature_home:feature_home_presentation` → `kg.timmitof.feature_home`
 */
fun Project.featureNamespacePrefix(): String {
    val parts = path.split(":").filter { it.isNotBlank() }
    if (parts.isEmpty()) error("Invalid project path: $path")

    val parentParts = parts.dropLast(1)
    return (listOf(ProjectConfig.BASE_NAMESPACE) + parentParts).joinToString(".")
}

/**
 * Путь к соседнему слою того же модуля.
 *
 * Примеры:
 * - `:keyboard:data` + "domain" → `:keyboard:domain`
 * - `:keyboard:suggestion:data` + "domain" → `:keyboard:suggestion:domain`
 * - `:feature_home:feature_home_data` + "domain" → `:feature_home:feature_home_domain`
 */
fun Project.featureModulePath(moduleName: String): String {
    val parts = path.split(":").filter { it.isNotBlank() }
    val rootModule = parts.getOrNull(0) ?: error("Invalid project path: $path")

    return if (rootModule.contains("feature")) {
        ":$rootModule:${rootModule}_$moduleName"
    } else {
        val parentPath = parts.dropLast(1).joinToString(":")
        ":$parentPath:$moduleName"
    }
}

val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")