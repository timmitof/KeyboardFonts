plugins {
    alias(libs.plugins.keyboardfonts.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "kg.timmitof.core.navigation"

    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlin.kotlinx.serialization.json)
}