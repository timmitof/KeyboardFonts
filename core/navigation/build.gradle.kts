plugins {
    alias(libs.plugins.keyboardfonts.kmp.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "kg.timmitof.core.navigation"
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(libs.jetbrains.navigation.compose)
        implementation(libs.kotlin.kotlinx.serialization.json)
    }
}
