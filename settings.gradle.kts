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
    include(
        "$featurePrefix:${featurePrefix}_data",
        "$featurePrefix:${featurePrefix}_domain",
        "$featurePrefix:${featurePrefix}_di",
        "$featurePrefix:${featurePrefix}_presentation"
    )
    project(":$featurePrefix:${featurePrefix}_data").projectDir = file("$featurePrefix/data")
    project(":$featurePrefix:${featurePrefix}_domain").projectDir = file("$featurePrefix/domain")
    project(":$featurePrefix:${featurePrefix}_di").projectDir = file("$featurePrefix/di")
    project(":$featurePrefix:${featurePrefix}_presentation").projectDir = file("$featurePrefix/presentation")
}

rootProject.name = "KeyboardFonts"

include(":app")

//Include Core
include(
    ":core:ui",
    ":core:navigation",
    ":core:common"
)