plugins { id("keyboardfonts.domain") }

dependencies {
    // Первый запуск ведёт в подключение, пока клавиатура не включена и не выбрана
    api(project(":keyboard:integration"))
}
