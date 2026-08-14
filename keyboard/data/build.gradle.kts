plugins { id("keyboardfonts.data") }

dependencies {
    implementation(project(":core:data"))
    implementation(project(":keyboard:font:domain"))

    testImplementation(libs.junit)
}
