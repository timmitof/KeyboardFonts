import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.keyboardfonts.application)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        FileInputStream(keystorePropertiesFile).use { load(it) }
    }
}

android {
    // Интерфейс — на языках клавиатуры (core/ui AppLocale): переводы библиотек на другие языки лишь раздувают resources.arsc.
    androidResources {
        localeFilters += listOf("ru", "en", "ky", "kk", "uz", "b+uz+Cyrl", "tg", "uk", "tr")
        // Список языков для выбора в системных настройках приложения (Android 13+); язык values/ — в resources.properties.
        generateLocaleConfig = true
        // Индекс опечаток Т9 (~1,5 МБ на язык) отображается в память прямо из APK — только если он не сжат.
        noCompress += "spell"
    }

    packaging {
        resources {
            excludes += listOf(
                "DebugProbesKt.bin",
                "kotlin-tooling-metadata.json",
                "META-INF/*.version",
                "META-INF/{AL2.0,LGPL2.1}",
            )
        }
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
                .takeIf { keystorePropertiesFile.exists() }
        }
    }
}

dependencies {
    implementation(project(":keyboard:integration"))
    implementation(project(":keyboard:engine"))
    // Hilt-модули собираются в графе приложения, поэтому di-слои подключаются здесь, а не в presentation.
    implementation(project(":keyboard:di"))
    implementation(project(":keyboard:suggestion:di"))
    implementation(project(":keyboard:font:di"))
    implementation(project(":keyboard:clipboard:di"))

    implementation(project(":feature_splash:feature_splash_di"))
    implementation(project(":feature_home:feature_home_di"))
    implementation(project(":feature_settings:feature_settings_di"))
    implementation(project(":feature_splash:feature_splash_presentation"))
    implementation(project(":feature_home:feature_home_presentation"))
    implementation(project(":feature_settings:feature_settings_presentation"))

    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:navigation"))
    implementation(project(":core:ui"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}