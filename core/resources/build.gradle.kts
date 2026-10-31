plugins {
    alias(libs.plugins.keyboardfonts.resources)
}

android {
    namespace = "kg.timmitof.core.resources"
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(libs.moko.resources)
        api(libs.moko.resources.compose)
    }
}

multiplatformResources {
    resourcesPackage.set("kg.timmitof.core.resources")
}
