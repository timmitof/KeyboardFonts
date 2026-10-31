plugins {
    id("keyboardfonts.data")
    // assets/languages.json собирается из каталога keyboard/build.gradle.kts.
    id("keyboardfonts.languageCatalog")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":core:data"))
    implementation(libs.kotlin.kotlinx.serialization.json)
    implementation(libs.androidx.datastore.preferences.core)
    implementation(project(":keyboard:font:domain"))

    testImplementation(libs.junit)
}
