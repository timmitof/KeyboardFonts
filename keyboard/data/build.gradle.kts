plugins {
    id("keyboardfonts.data")
    // assets/languages.json собирается из каталога keyboard/build.gradle.kts.
    id("keyboardfonts.languageCatalog")
}

dependencies {
    implementation(project(":core:data"))
    implementation(libs.gson)
    implementation(libs.androidx.datastore.preferences)
    implementation(project(":keyboard:font:domain"))

    testImplementation(libs.junit)
}
