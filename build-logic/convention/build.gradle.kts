plugins {
    `kotlin-dsl`
}


dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("keyboardfontsApplication") {
            id = "keyboardfonts.application"
            implementationClass = "kg.timmitof.build_logic.convention.plugins.AndroidApplicationPlugin"
        }

        register("keyboardfontsLibrary") {
            id = "keyboardfonts.library"
            implementationClass = "kg.timmitof.build_logic.convention.plugins.AndroidLibraryPlugin"
        }
    }
}