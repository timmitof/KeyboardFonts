plugins { id("keyboardfonts.domain") }

dependencies {
    // Настройки хранит клавиатура — фича только показывает их пользователю
    api(project(":keyboard:domain"))
    api(project(":keyboard:font:domain"))

    // История буфера: закреплённые записи видны прямо во вкладке «Буфер»
    api(project(":keyboard:clipboard:domain"))

    // Контракт с системными настройками: включена ли клавиатура и выбрана ли она
    api(project(":keyboard:integration"))
}
