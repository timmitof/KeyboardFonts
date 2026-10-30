plugins {
    alias(libs.plugins.keyboardfonts.kmp.library)
}

android {
    namespace = "kg.timmitof.keyboard.integration"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Flow — часть контракта, поэтому корутины видны потребителям.
            api(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }
    }
}
