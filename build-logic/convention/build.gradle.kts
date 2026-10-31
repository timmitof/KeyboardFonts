plugins {
    `kotlin-dsl`
}


dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    // Нужен только тип задачи генерации; транзитивный kotlin-stdlib 2.3 не читается kotlin-dsl Gradle 8.13.
    compileOnly(libs.moko.resources.generator) { isTransitive = false }
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
        register("keyboardfontsKmpLibrary") {
            id = "keyboardfonts.kmp.library"
            implementationClass = "kg.timmitof.build_logic.convention.plugins.KmpLibraryPlugin"
        }
        register("keyboardfontsKmpCompose") {
            id = "keyboardfonts.kmp.compose"
            implementationClass = "kg.timmitof.build_logic.convention.plugins.KmpComposePlugin"
        }
        register("keyboardfontsResources") {
            id = "keyboardfonts.resources"
            implementationClass = "kg.timmitof.build_logic.convention.plugins.ResourcesPlugin"
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
        register("languagesPlugin") {
            id = "keyboardfonts.languages"
            implementationClass = "kg.timmitof.build_logic.convention.plugins.LanguagesPlugin"
        }
        register("languageCatalogPlugin") {
            id = "keyboardfonts.languageCatalog"
            implementationClass = "kg.timmitof.build_logic.convention.plugins.LanguageCatalogPlugin"
        }
    }
}