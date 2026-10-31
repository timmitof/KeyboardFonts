plugins { id("keyboardfonts.di") }

kotlin {
    sourceSets.commonMain.dependencies {
        // Koin описывает конструкторы data-классов, поэтому типы их зависимостей должны быть видны здесь.
        implementation(project(":keyboard:data"))
    }
}
