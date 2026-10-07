"""
Список всех словоформ русского языка из словаря OpenCorpora (CC BY-SA 3.0, http://opencorpora.org/dict.php).

Из него плагин `keyboardfonts.dictionaries` собирает фильтр Блума «это настоящее слово»: такие формы
Т9 не исправляет автозаменой и не выбрасывает из словаря как опечатки.

Выход — `keyboard/suggestion/data/dictionaries/ru_forms.txt.gz`: по слову в строке, нижний регистр,
«ё» сведена к «е», без повторов, отсортировано. Файл коммитится: сборка не должна ходить в сеть,
а без него русский соберётся без фильтра.

Запуск из корня репозитория (нужен только Python 3, без пакетов):

    python -I tools/opencorpora_forms.py

Уже скачанный архив можно передать, чтобы не качать заново:

    python -I tools/opencorpora_forms.py --dump путь/к/dict.opcorpora.txt.zip

Дамп (~40 МБ) качается во временную папку и в репозиторий не попадает.

Если opencorpora.org недоступен, тот же словарь есть на PyPI в сборке pymorphy3
(pymorphy3-dicts-ru собирается из дампа OpenCorpora):

    python -m venv .venv && .venv/Scripts/pip install pymorphy3 pymorphy3-dicts-ru
    .venv/Scripts/python -I tools/opencorpora_forms.py --source pymorphy
"""

import argparse
import gzip
import io
import os
import shutil
import tempfile
import urllib.request
import zipfile

DUMP_URL = "https://opencorpora.org/files/export/dict/dict.opcorpora.txt.zip"
DEFAULT_OUTPUT = os.path.join("keyboard", "suggestion", "data", "dictionaries", "ru_forms.txt.gz")


def download(target):
    print(f"Качаю {DUMP_URL} …")
    with urllib.request.urlopen(DUMP_URL) as response, open(target, "wb") as out:
        shutil.copyfileobj(response, out)


def pymorphy_forms():
    """Все формы всех лемм из словаря pymorphy3 — это скомпилированный дамп OpenCorpora."""
    import pymorphy3

    for word, *_ in pymorphy3.MorphAnalyzer().dictionary.iter_known_words():
        yield word.lower().replace("ё", "е")


def forms(dump):
    """
    Формат дампа: номер леммы, затем строки `ФОРМА<TAB>граммемы`, лемма от леммы — пустой строкой.
    Берём первую колонку всех строк с табуляцией.
    """
    with zipfile.ZipFile(dump) as archive:
        name = next(n for n in archive.namelist() if n.endswith(".txt"))
        with archive.open(name) as raw:
            for line in io.TextIOWrapper(raw, encoding="utf-8"):
                tab = line.find("\t")
                if tab > 0:
                    yield line[:tab].strip().lower().replace("ё", "е")


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--source", choices=("opencorpora", "pymorphy"), default="opencorpora")
    parser.add_argument("--dump", help="готовый dict.opcorpora.txt.zip вместо скачивания")
    parser.add_argument("--output", default=DEFAULT_OUTPUT)
    args = parser.parse_args()

    if args.source == "pymorphy":
        unique = sorted({form for form in pymorphy_forms() if form})
    else:
        with tempfile.TemporaryDirectory() as temp:
            dump = args.dump
            if dump is None:
                dump = os.path.join(temp, "dict.opcorpora.txt.zip")
                download(dump)
            unique = sorted({form for form in forms(dump) if form})

    # mtime=0 — одинаковый вход даёт побайтно одинаковый архив, git не видит ложных изменений.
    with open(args.output, "wb") as out, \
            gzip.GzipFile(filename="", mode="wb", fileobj=out, compresslevel=9, mtime=0) as packed:
        packed.write(("\n".join(unique) + "\n").encode("utf-8"))

    print(f"{len(unique)} форм -> {args.output} ({os.path.getsize(args.output) // 1024} КБ)")


if __name__ == "__main__":
    main()
