plugins {
    alias(libs.plugins.keyboardfonts.kmp.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "kg.timmitof.core.data"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.room.runtime)
            api(libs.androidx.datastore.preferences.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        iosMain.dependencies {
            implementation(libs.sqlite.bundled)
        }
    }
}

// Room генерирует код базы для каждой платформы отдельно.
dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}
