plugins {
    alias(libs.plugins.keyboardfonts.library)
}

android {
    namespace = "kg.timmitof.keyboard.integration"
}

dependencies {
    implementation(project(":keyboard:engine"))
}