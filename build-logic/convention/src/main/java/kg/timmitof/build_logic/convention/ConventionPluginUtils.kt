package kg.timmitof.build_logic.convention

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/** Namespace модуля: базовый префикс + родительские сегменты пути (`:keyboard:data` → `kg.timmitof.keyboard`). */
fun Project.featureNamespacePrefix(): String {
    val parts = path.split(":").filter { it.isNotBlank() }
    if (parts.isEmpty()) error("Invalid project path: $path")

    val parentParts = parts.dropLast(1)
    return (listOf(ProjectConfig.BASE_NAMESPACE) + parentParts).joinToString(".")
}

/** Путь к соседнему слою: `:keyboard:data` + "domain" → `:keyboard:domain`. */
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