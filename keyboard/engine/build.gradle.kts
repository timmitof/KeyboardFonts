plugins {
    alias(libs.plugins.keyboardfonts.library)
}

android {
    namespace = "kg.timmitof.keyboard.engine"
}

dependencies {
    implementation(project(":keyboard:presentation"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
}