plugins {
    id("keyboardfonts.data")
    // Какие словари собирать — блок dictionary(...) языка в каталоге keyboard/build.gradle.kts.
    id("keyboardfonts.dictionaries")
}

dependencies {
    implementation(project(":keyboard:data"))

    testImplementation(libs.junit)
}
