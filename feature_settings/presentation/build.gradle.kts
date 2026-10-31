plugins { id("keyboardfonts.presentation") }

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":keyboard:presentation"))
        implementation(libs.filekit.dialogs.compose)
    }
}

compose.resources {
    packageOfResClass = "kg.timmitof.feature_settings.presentation.resources"
}
