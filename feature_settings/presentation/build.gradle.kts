plugins { id("keyboardfonts.presentation") }

dependencies {
    // Предпросмотр рисуется теми же клавишами и цветами, что и живая клавиатура
    implementation(project(":keyboard:presentation"))
}
