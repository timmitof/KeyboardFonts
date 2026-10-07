plugins {
    alias(libs.plugins.keyboardfonts.library)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "kg.timmitof.keyboard.integration"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}