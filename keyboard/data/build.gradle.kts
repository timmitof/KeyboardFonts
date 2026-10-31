plugins {
    id("keyboardfonts.data")
    // assets/languages.json собирается из каталога keyboard/build.gradle.kts.
    id("keyboardfonts.languageCatalog")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:data"))
            implementation(project(":keyboard:font:domain"))
            implementation(libs.kotlin.kotlinx.serialization.json)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
