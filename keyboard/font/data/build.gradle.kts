plugins { id("keyboardfonts.data") }

dependencies {
    implementation(project(":keyboard:data"))
    implementation(libs.androidx.datastore.preferences)
}
