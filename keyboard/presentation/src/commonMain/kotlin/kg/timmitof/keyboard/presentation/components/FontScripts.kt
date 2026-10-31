package kg.timmitof.keyboard.presentation.components

import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.font.domain.model.FontScript
import kg.timmitof.keyboard.font.domain.model.KeyboardFont

/** Нелатинские языки каталога пока только кириллические; нет языка — латиница. */
val KeyboardLanguage?.fontScript: FontScript
    get() = if (this?.isLatin == false) FontScript.CYRILLIC else FontScript.LATIN

/** Образец для плашки шрифта — на алфавите раскладки. */
val FontScript.fontSample: String
    get() = when (this) {
        FontScript.LATIN -> "Abc"
        FontScript.CYRILLIC -> "Абв"
    }

/** Короткий образец для кнопки шрифта в топбаре. */
val FontScript.fontShortSample: String
    get() = when (this) {
        FontScript.LATIN -> "Aa"
        FontScript.CYRILLIC -> "Аа"
    }

/** Стили, которые реально работают на алфавите, — остальные на панели не показываем. */
fun List<KeyboardFont>.supporting(script: FontScript): List<KeyboardFont> =
    if (all { it.supports(script) }) this else filter { it.supports(script) }

/** Выбранный стиль без поддержки алфавита не применяется, но и не сбрасывается: вернёшься на EN — он на месте. */
fun KeyboardFont.orDefaultFor(script: FontScript): KeyboardFont =
    if (supports(script)) this else KeyboardFont.Default
