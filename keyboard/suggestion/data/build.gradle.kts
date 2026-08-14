plugins { id("keyboardfonts.data") }

dependencies {
    implementation(project(":keyboard:data"))
    implementation(libs.symspellkt)

    testImplementation(libs.junit)
}
