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
        register("presentationPlugin") {
            id = "keyboardfonts.presentation"
            implementationClass = "kg.timmitof.build_logic.convention.plugins.PresentationPlugin"
        }
        register("diPlugin") {
            id = "keyboardfonts.di"
            implementationClass = "kg.timmitof.build_logic.convention.plugins.DiPlugin"
        }
        register("domainPlugin") {
            id = "keyboardfonts.domain"
            implementationClass = "kg.timmitof.build_logic.convention.plugins.DomainPlugin"
        }
        register("dataPlugin") {
            id = "keyboardfonts.data"
            implementationClass = "kg.timmitof.build_logic.convention.plugins.DataPlugin"
        }
        register("dictionariesPlugin") {
            id = "keyboardfonts.dictionaries"
            implementationClass = "kg.timmitof.build_logic.convention.plugins.DictionariesPlugin"
        }
    }
}