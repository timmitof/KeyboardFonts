"""Собирает поисковый словарь эмодзи (ru + en) из аннотаций CLDR под наш каталог.

Запуск: python tools/emoji/build_annotations.py
Результат: keyboard/data/src/main/assets/emoji/annotations.tsv

Формат строки: эмодзи \t русские|ключи \t english|keys — первым идёт название.
Данные CLDR скачиваются во временную папку и в репозиторий не попадают.
"""
import json
import pathlib
import re
import sys
import tempfile
import urllib.request

PROJECT = pathlib.Path(__file__).resolve().parents[2]
CATALOG = PROJECT / "keyboard/data/src/main/java/kg/timmitof/keyboard/data/emoji/catalog"
OUT = PROJECT / "keyboard/data/src/main/assets/emoji/annotations.tsv"
CACHE = pathlib.Path(tempfile.gettempdir()) / "cldr-emoji-annotations"

BASE_URL = "https://raw.githubusercontent.com/unicode-org/cldr-json/main/cldr-json"
SOURCES = {
    "ann": ("cldr-annotations-full/annotations", "annotations"),
    "der": ("cldr-annotations-derived-full/annotationsDerived", "annotationsDerived"),
}

VS16 = "️"
MAX_KEYWORDS = 10


def download(kind, locale):
    """Файл аннотаций CLDR — из кэша или из сети."""
    folder, _ = SOURCES[kind]
    CACHE.mkdir(parents=True, exist_ok=True)
    path = CACHE / f"{kind}_{locale}.json"
    if not path.exists():
        url = f"{BASE_URL}/{folder}/{locale}/annotations.json"
        print(f"качаю {url}")
        urllib.request.urlretrieve(url, path)
    return path


def load(locale):
    """Аннотации и производные (ZWJ-последовательности, флаги) одного языка."""
    merged = {}
    for kind, (_, root) in SOURCES.items():
        data = json.loads(download(kind, locale).read_text(encoding="utf-8"))
        for emoji, entry in data[root]["annotations"].items():
            merged.setdefault(emoji, entry)
    return merged


def catalog_emojis():
    """Эмодзи в порядке каталога — строковые литералы из файлов категорий."""
    found = []
    for path in sorted(CATALOG.glob("*.kt")):
        found.extend(re.findall(r'"([^"]+)"', path.read_text(encoding="utf-8")))

    unique = []
    seen = set()
    for emoji in found:
        if emoji not in seen:
            seen.add(emoji)
            unique.append(emoji)
    return unique


def lookup(table, emoji):
    """CLDR хранит часть эмодзи без вариационного селектора, часть — с ним."""
    for candidate in (emoji, emoji.replace(VS16, ""), emoji + VS16):
        if candidate in table:
            return table[candidate]
    return None


def keywords(entry):
    """Название эмодзи первым, дальше синонимы — без повторов."""
    if not entry:
        return []
    result = []
    for word in list(entry.get("tts", [])) + list(entry.get("default", [])):
        word = word.strip().lower().replace("|", " ")
        if word and word not in result:
            result.append(word)
    return result[:MAX_KEYWORDS]


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    ru, en = load("ru"), load("en")
    lines, missing = [], []

    for emoji in catalog_emojis():
        ru_words = keywords(lookup(ru, emoji))
        en_words = keywords(lookup(en, emoji))
        if not ru_words and not en_words:
            missing.append(emoji)
            continue
        lines.append(f"{emoji}\t{'|'.join(ru_words)}\t{'|'.join(en_words)}")

    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text("\n".join(lines) + "\n", encoding="utf-8", newline="\n")

    print(f"строк: {len(lines)}, размер: {OUT.stat().st_size // 1024} КБ")
    if missing:
        print(f"без ключевых слов: {len(missing)} -> {''.join(missing)}")


if __name__ == "__main__":
    sys.exit(main())
