plugins {
    alias(libs.plugins.keyboardfonts.library)
}

android {
    namespace = "kg.timmitof.keyboard.integration"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.android)
}