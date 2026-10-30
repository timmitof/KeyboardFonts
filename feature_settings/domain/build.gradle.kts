plugins { id("keyboardfonts.domain") }

kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":keyboard:domain"))
        api(project(":keyboard:font:domain"))

        api(project(":keyboard:clipboard:domain"))

        api(project(":keyboard:integration"))
    }
}
