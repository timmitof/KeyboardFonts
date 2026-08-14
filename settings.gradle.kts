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
    ":core:navigation",
    ":core:ui",
)

//Include Keyboard
include(
    ":keyboard:integration",
    ":keyboard:engine",
    ":keyboard:presentation",
    ":keyboard:data",
    ":keyboard:domain",
    ":keyboard:di",
)

fun includeKeyboardModule(name: String) {
    val layers = listOf("data", "domain", "di")
    layers.forEach { layer ->
        val layerDir = file("keyboard/$name/$layer")
        if (layerDir.exists()) {
            val modulePath = ":keyboard:$name:$layer"
            include(modulePath)
            project(modulePath).projectDir = layerDir
            println("Included $modulePath")
        }
    }
}

includeKeyboardModule("suggestion")
includeKeyboardModule("font")
includeKeyboardModule("clipboard")

includeFeature("home")
includeFeature("settings")
includeFeature("splash")
