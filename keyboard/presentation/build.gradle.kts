plugins { id("keyboardfonts.presentation") }

dependencies {
    implementation(project(":keyboard:suggestion:domain"))
    implementation(project(":keyboard:font:domain"))
    implementation(project(":keyboard:clipboard:domain"))
    implementation(project(":keyboard:translate:domain"))

    testImplementation(libs.junit)
}