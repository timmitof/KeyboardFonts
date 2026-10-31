plugins {
    alias(libs.plugins.keyboardfonts.kmp.compose)
}

android {
    namespace = "kg.timmitof.core.ui"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            implementation(project(":core:navigation"))
            implementation(project(":core:resources"))

            implementation(libs.jetbrains.compose.ui.backhandler)
            implementation(libs.jetbrains.compose.material.icons.core)
            implementation(libs.jetbrains.lifecycle.viewmodel.compose)

            implementation(libs.orbit.core)
            implementation(libs.orbit.compose)
            implementation(libs.orbit.viewmodel)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            // На Android — та же material3, что и раньше (1.5 alpha): expressive-анимации и отступы шапки.
            implementation(libs.androidx.material3)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "kg.timmitof.core.ui.resources"
}
