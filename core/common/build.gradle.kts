plugins {
    alias(libs.plugins.keyboardfonts.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "kg.timmitof.core.common"

    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
}