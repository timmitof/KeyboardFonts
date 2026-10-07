plugins {
    id("keyboardfonts.data")
    id("keyboardfonts.dictionaries")
}

// Исходники — частотные списки в dictionaries/ (hermitdave/FrequencyWords, CC BY-SA 4.0).
// Пока списка языка нет, используется ручной словарь из src/main/assets/dictionaries.
// Словоформы русского (ru_forms.txt.gz) — словарь OpenCorpora, CC BY-SA 3.0, http://opencorpora.org;
// пересборка списка — tools/opencorpora_forms.py.
dictionaries {
    language("ru_ru", source = "ru_full.txt") {
        alphabet = "абвгдежзийклмнопрстуфхцчшщъыьэюя"
        singleLetters = "авикосуя"
        forms = "ru_forms.txt.gz"
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
