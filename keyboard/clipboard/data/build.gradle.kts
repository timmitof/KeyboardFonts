plugins { id("keyboardfonts.data") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:data"))
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
