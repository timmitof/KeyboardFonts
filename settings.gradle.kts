pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

fun includeFeature(name: String) {
    val featurePrefix = "feature_$name"
    val featureDir = file(featurePrefix)

    // Слои feature-модуля
    val layers = listOf("data", "domain", "di", "presentation")

    layers.forEach { layer ->
        val layerDir = File(featureDir, layer)
        if (layerDir.exists()) {
            val modulePath = ":$featurePrefix:${featurePrefix}_$layer"
            include(modulePath)
            project(modulePath).projectDir = layerDir
            println("Included $modulePath")
        } else {
            println("Skip $featurePrefix/$layer (not found)")
        }
    }
}

rootProject.name = "KeyboardFonts"

include(":app")

//Include Core
include(
    ":core:common",
    ":core:data",
    ":core:domain",
    ":core:navigation",
    ":core:ui",
)
includeFeature("home")
includeFeature("splash")