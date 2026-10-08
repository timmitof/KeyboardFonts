"""
Урезает шрифты интерфейса до нужных глифов и кладёт их в `core/common/src/main/res/font/`.

Полные исходники лежат рядом, в `tools/fonts/source/`, и в APK не попадают: в ресурсы идут только
урезанные копии. Все шрифты под SIL Open Font License 1.1 (тексты лицензий — `source/OFL-*.txt`);
Reserved Font Name у них не объявлено, поэтому урезанные версии можно не переименовывать.
Копирайт и ссылка на лицензию остаются в таблице `name` каждого файла — этого OFL достаточно.

Что оставляем: латиницу (с расширениями — их дают акцентные варианты клавиш), кириллицу, цифры,
пунктуацию и символы, которые реально есть в строках, раскладках и коде интерфейса (стрелки, ✓, ★,
валюты, дроби и т. п.). Если глифа нет в самом шрифте, он просто не попадёт в результат — Android
возьмёт его из системного шрифта, как и раньше. Выкидываем всё остальное (у Poppins это деванагари —
почти половина файла).

Запуск из корня репозитория (нужен fontTools: `pip install fonttools`):

    python -I tools/fonts/subset_fonts.py

Новый шрифт — новая строка в FONTS; новый символ в интерфейсе — добавить его диапазон в UNICODES
и перезапустить скрипт.
"""

import sys
from dataclasses import dataclass, field
from pathlib import Path

from fontTools import subset
from fontTools.ttLib import TTFont
from fontTools.varLib.instancer import instantiateVariableFont

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "tools/fonts/source"
OUTPUT = ROOT / "core/common/src/main/res/font"

# Диапазоны кодовых точек, которые нужны интерфейсу (включительно).
UNICODES = [
    (0x0020, 0x007E),  # ASCII: латиница, цифры, пунктуация
    (0x00A0, 0x024F),  # Latin-1 + Latin Extended-A/B: акцентные варианты клавиш, «», ©, °, ×, ÷
    (0x0250, 0x02FF),  # ə (азербайджанский) и отдельные знаки диакритик: ˆ ˇ ˘ ˙ ˚ ˛ ˜ ˝ ʼ
    (0x0300, 0x036F),  # комбинируемые диакритики (для составных букв)
    (0x03A0, 0x03A0),  # Π — раскладка символов
    (0x03C0, 0x03C0),  # π — раскладка символов
    (0x0400, 0x04FF),  # кириллица
    (0x1E00, 0x1EFF),  # Latin Extended Additional (вьетнамские и прочие варианты клавиш)
    (0x2000, 0x206F),  # общая пунктуация: – — ‘ ’ “ ” „ … • ‰ ′ ″
    (0x2070, 0x209F),  # надстрочные/подстрочные: ⁿ
    (0x20A0, 0x20CF),  # валюты: €, ₽, ₸ …
    (0x2100, 0x218F),  # буквоподобные и дроби: №, ™, ⅓, ⅔
    (0x2190, 0x21FF),  # стрелки
    (0x2200, 0x22FF),  # математика: −, √, ∞, ≈, ≠, ∆, ∏, ∅
    (0x2300, 0x23FF),  # ⌫
    (0x2423, 0x2423),  # ␣
    (0x25A0, 0x25FF),  # геометрия: ▾
    (0x2600, 0x26FF),  # ★ ♠ ♣ ♥ ♦ ♪
    (0x2700, 0x27BF),  # ✓ ✕
    (0xFB00, 0xFB06),  # латинские лигатуры fi/fl
]


@dataclass(frozen=True)
class FontSpec:
    """Один шрифт ресурсов: исходник, имя в res/font и (для вариативных) координаты осей."""

    source: str
    output: str
    axes: dict = field(default_factory=dict)


FONTS = [
    FontSpec("poppins_regular.ttf", "poppins_regular.ttf"),
    FontSpec("poppins_semi_bold.ttf", "poppins_semi_bold.ttf"),
    FontSpec("poppins_bold.ttf", "poppins_bold.ttf"),
    FontSpec("poppins_extra_bold.ttf", "poppins_extra_bold.ttf"),
    # Надпись сплэша. wght=1000 — самый жирный край оси Nunito: по ширине и толщине штриха он ближе
    # всего к прежнему SF Pro Rounded Black (ширина «KeyboardFonts» 7.54 em против 7.59).
    FontSpec("Nunito[wght].ttf", "nunito_black.ttf", axes={"wght": 1000}),
]


def subset_options() -> subset.Options:
    options = subset.Options()
    options.name_IDs = ["*"]  # копирайт и лицензия (OFL требует их сохранить)
    options.name_languages = ["*"]
    options.notdef_outline = True
    options.layout_features = ["*"]  # кернинг, лигатуры, locl для кириллицы — как в исходнике
    return options


def unicodes() -> list[int]:
    return [code for start, end in UNICODES for code in range(start, end + 1)]


def build(spec: FontSpec) -> None:
    font = TTFont(SOURCE / spec.source)
    if spec.axes:
        font = instantiateVariableFont(font, spec.axes)

    subsetter = subset.Subsetter(subset_options())
    subsetter.populate(unicodes=unicodes())
    subsetter.subset(font)

    target = OUTPUT / spec.output
    font.save(target)
    before = (SOURCE / spec.source).stat().st_size
    print(f"{spec.output}: {before / 1024:.0f} КБ -> {target.stat().st_size / 1024:.0f} КБ")


def main() -> None:
    sys.stdout.reconfigure(encoding="utf-8")
    OUTPUT.mkdir(parents=True, exist_ok=True)
    for spec in FONTS:
        build(spec)


if __name__ == "__main__":
    main()
