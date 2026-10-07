plugins { id("keyboardfonts.data") }

dependencies {
    implementation(project(":core:data"))
    implementation(libs.gson)
    implementation(libs.androidx.datastore.preferences)
    implementation(project(":keyboard:font:domain"))

    testImplementation(libs.junit)
}
