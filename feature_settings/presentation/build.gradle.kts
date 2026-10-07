plugins { id("keyboardfonts.presentation") }

dependencies {
    implementation(project(":keyboard:presentation"))

    implementation(libs.androidx.activity.compose)
}
