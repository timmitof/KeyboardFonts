plugins {
    id("keyboardfonts.data")
    id("keyboardfonts.dictionaries")
}

// Исходники — частотные списки в dictionaries/ (hermitdave/FrequencyWords, CC BY-SA 4.0).
// Пока списка языка нет, используется ручной словарь из src/main/assets/dictionaries.
dictionaries {
    language("ru_ru", source = "ru_full.txt") {
        alphabet = "абвгдежзийклмнопрстуфхцчшщъыьэюя"
        singleLetters = "авикосуя"
        fold('ё', 'е')
    }
    language("en_us", source = "en_full.txt") {
        alphabet = "abcdefghijklmnopqrstuvwxyz'"
        singleLetters = "aio"
    }
}

dependencies {
    implementation(project(":keyboard:data"))
    implementation(libs.symspellkt)

    testImplementation(libs.junit)
}
