plugins { id("keyboardfonts.domain") }

dependencies {
    // Контракт с системными настройками клавиатуры (включена/выбрана, переход в настройки)
    api(project(":keyboard:integration"))
}
