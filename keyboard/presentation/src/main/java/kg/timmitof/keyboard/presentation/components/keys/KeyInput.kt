package kg.timmitof.keyboard.presentation.components.keys

import kg.timmitof.keyboard.domain.model.KeyCharacter
import kg.timmitof.keyboard.domain.model.KeyboardKey
import kg.timmitof.keyboard.domain.model.KeyboardLayout

/** Что вводит клавиша: свой [KeyboardKey.Character.output] или подпись в обоих регистрах. */
internal fun KeyboardKey.Character.input(): KeyCharacter =
    output?.let(::KeyCharacter) ?: KeyCharacter(labelLower, labelUpper)

/** Буква клавиши в нижнем регистре; служебные и многосимвольные клавиши буквой не считаются. */
internal fun KeyboardKey.Character.letter(): Char? =
    takeUnless { it.isSpecial }?.let { it.output ?: it.labelLower }?.singleOrNull()?.lowercaseChar()

/** Клавиша буквы [letter] на раскладке. */
internal fun KeyboardLayout.letterKey(letter: Char): KeyboardKey.Character? =
    rows.firstNotNullOfOrNull { row ->
        row.firstOrNull { key -> key is KeyboardKey.Character && key.letter() == letter } as KeyboardKey.Character?
    }
