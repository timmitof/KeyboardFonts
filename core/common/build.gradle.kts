plugins {
    alias(libs.plugins.keyboardfonts.kmp.compose)
}

android {
    namespace = "kg.timmitof.core.common"
}

kotlin {
    sourceSets.commonMain.dependencies {
        // Res отдаёт DrawableResource и FontResource — потребителям нужен API ресурсов.
        api(libs.jetbrains.compose.components.resources)
    }
}

// Логотип и шрифты бренда — общие для приложения, сплэша и клавиатуры.
compose.resources {
    publicResClass = true
    packageOfResClass = "kg.timmitof.core.common.resources"
}
