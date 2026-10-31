plugins { id("keyboardfonts.presentation.android") }

dependencies {
    implementation(project(":keyboard:presentation"))

    implementation(libs.androidx.activity.compose)
}
