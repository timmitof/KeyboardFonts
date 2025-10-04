plugins {
    alias(libs.plugins.keyboardfonts.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "kg.timmitof.navigation"

    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlin.kotlinx.serialization.json)
}