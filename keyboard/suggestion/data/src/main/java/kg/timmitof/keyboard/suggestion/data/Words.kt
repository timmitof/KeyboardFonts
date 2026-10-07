package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.keyboard.data.font.FontDecoder

/** Форма слова как в словаре: без стилизации шрифтом, в нижнем регистре, без «ё». */
internal fun String.toDictionaryForm(): String = FontDecoder.decode(this).foldToDictionary()

internal fun String.foldToDictionary(): String = lowercase().replace('ё', 'е')

internal fun String.isLearnable(): Boolean =
    length in MIN_LEARNABLE_LENGTH..MAX_LEARNABLE_LENGTH && all { it.isLetter() || it == '\'' }

private const val MIN_LEARNABLE_LENGTH = 2
private const val MAX_LEARNABLE_LENGTH = 24
