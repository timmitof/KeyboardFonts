plugins { id("keyboardfonts.languages") }

// Каталог языков клавиатуры — единственный источник правды: из него собираются assets/languages.json
// (keyboard:data) и словари Т9 (keyboard:suggestion:data). Порядок записей — порядок каталога,
// первая — запасной язык по умолчанию. Как добавить язык — CLAUDE.md, «Языки клавиатуры».
//
// Частотные списки — keyboard/suggestion/data/dictionaries (hermitdave/FrequencyWords, CC BY-SA 4.0).
// Словоформы русского (ru_forms.txt.gz) — словарь OpenCorpora, CC BY-SA 3.0, http://opencorpora.org;
// пересборка списка — tools/opencorpora_forms.py.
keyboardLanguages {
    language("ru_ru") {
        name = "Русский"
        alphabet = "абвгдежзийклмнопрстуфхцчшщъыьэюя"
        dictionary(source = "ru_full.txt") {
            singleLetters = "авикосуя"
            forms = "ru_forms.txt.gz"
            formsMinOccurrences = 1
            formsFalsePositiveRate = 0.03
            fold('ё', 'е')
        }
    }
    language("en_us") {
        name = "English"
        isLatin = true
        alphabet = "abcdefghijklmnopqrstuvwxyz'"
        dictionary(source = "en_full.txt") {
            singleLetters = "aio"
        }
    }
    language("ky_kg") {
        name = "Кыргызча"
        alphabet = "абвгдеёжзийклмнңоөпрстуүфхцчшщъыьэюя"
    }
    language("kk_kz") {
        name = "Қазақша"
        alphabet = "аәбвгғдеёжзийкқлмнңоөпрстуұүфхһцчшщъыіьэюя"
    }
}
