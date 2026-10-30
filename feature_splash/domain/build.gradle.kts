plugins { id("keyboardfonts.domain") }

kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":keyboard:integration"))
    }
}
