plugins {
    alias(libs.plugins.keyboardfonts.kmp.compose)
    // Своих строк нет: moko здесь, чтобы упаковать в фреймворк строки из :core:resources.
    alias(libs.plugins.moko.resources)
}

android {
    namespace = "kg.timmitof.shared"
}

kotlin {
    // Динамический фреймворк: ресурсы moko упаковываются в него сами, и его же подключит клавиатура iOS.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = false
            binaryOption("bundleId", "kg.timmitof.shared")
        }
    }

    sourceSets.commonMain.dependencies {
        implementation(project(":core:data"))
        implementation(project(":core:navigation"))
        implementation(project(":core:ui"))
        implementation(project(":keyboard:integration"))

        implementation(project(":keyboard:di"))
        implementation(project(":keyboard:font:di"))
        implementation(project(":keyboard:suggestion:di"))
        implementation(project(":keyboard:clipboard:di"))

        implementation(project(":feature_splash:feature_splash_di"))
        implementation(project(":feature_home:feature_home_di"))
        implementation(project(":feature_settings:feature_settings_di"))
        implementation(project(":feature_splash:feature_splash_presentation"))
        implementation(project(":feature_home:feature_home_presentation"))
        implementation(project(":feature_settings:feature_settings_presentation"))

        implementation(libs.koin.core)
    }
}

multiplatformResources {
    resourcesPackage.set("kg.timmitof.shared")
}

/**
 * Ассеты клавиатуры — раскладки, каталог языков, словари — в бандл iOS-приложения, в `assets/`, как на Android
 * (их читает `IosAssetTextLoader`). Запускается вместе со сборкой фреймворка из Xcode.
 */
val syncKeyboardAssetsForXcode = tasks.register<Sync>("syncKeyboardAssetsForXcode") {
    val keyboardData = project(":keyboard:data")
    val suggestionData = project(":keyboard:suggestion:data")

    from(keyboardData.layout.projectDirectory.dir("src/androidMain/assets"))
    from(keyboardData.layout.buildDirectory.dir("generated/languageCatalog"))
    from(suggestionData.layout.projectDirectory.dir("src/androidMain/assets"))
    from(suggestionData.layout.buildDirectory.dir("generated/dictionaries"))
    dependsOn(":keyboard:data:generateLanguageCatalog", ":keyboard:suggestion:data:generateDictionaries")

    into(
        providers.environmentVariable("BUILT_PRODUCTS_DIR")
            .zip(providers.environmentVariable("CONTENTS_FOLDER_PATH")) { products, contents -> "$products/$contents/assets" }
    )
}

tasks.matching { it.name == "embedAndSignAppleFrameworkForXcode" }.configureEach {
    dependsOn(syncKeyboardAssetsForXcode)
}
