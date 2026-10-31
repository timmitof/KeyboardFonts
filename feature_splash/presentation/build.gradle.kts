plugins { id("keyboardfonts.presentation") }

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":core:common"))
    }
}
