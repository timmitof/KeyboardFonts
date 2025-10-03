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
        register("applicationPlugin") {
            id = "timmitof.application"
            implementationClass = "kg.timmitof.build_logic.convention.plugins.AndroidApplicationPlugin"
        }
    }
}