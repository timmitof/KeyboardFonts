plugins {
    id("keyboardfonts.data")
    // Какие словари собирать — блок dictionary(...) языка в каталоге keyboard/build.gradle.kts.
    id("keyboardfonts.dictionaries")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:data"))
            implementation(project(":keyboard:data"))
            implementation(libs.kotlinx.atomicfu)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

// GeneratedDictionariesTest читает словари, которые собирает generateDictionaries.
tasks.withType<Test>().configureEach {
    dependsOn("generateDictionaries")
}
