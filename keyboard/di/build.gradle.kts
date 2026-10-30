plugins { id("keyboardfonts.di") }

dependencies {
    // Koin описывает конструкторы data-классов, поэтому типы их зависимостей должны быть видны здесь.
    implementation(project(":core:data"))
}
