plugins { id("keyboardfonts.data") }

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":keyboard:data"))
        implementation(libs.androidx.datastore.preferences.core)
    }
}
