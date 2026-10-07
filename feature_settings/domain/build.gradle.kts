plugins { id("keyboardfonts.domain") }

dependencies {
    api(project(":keyboard:domain"))
    api(project(":keyboard:font:domain"))

    api(project(":keyboard:clipboard:domain"))

    api(project(":keyboard:integration"))
}
