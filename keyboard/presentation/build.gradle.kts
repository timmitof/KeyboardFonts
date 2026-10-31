plugins { id("keyboardfonts.presentation") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":keyboard:suggestion:domain"))
            implementation(project(":keyboard:font:domain"))
            implementation(project(":keyboard:clipboard:domain"))
            implementation(libs.jetbrains.compose.ui.backhandler)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
        }
    }
}

compose.resources {
    packageOfResClass = "kg.timmitof.keyboard.presentation.resources"
}
