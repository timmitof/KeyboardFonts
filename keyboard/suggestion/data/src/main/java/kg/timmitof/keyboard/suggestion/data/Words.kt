package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.keyboard.data.font.FontDecoder

/**
 * Приведение слова к той форме, в которой оно лежит в словаре:
 * без стилизации шрифтом, в нижнем регистре и без «ё».
 */
internal fun String.toDictionaryForm(): String = FontDecoder.decode(this).foldToDictionary()

/** То же самое для текста, со стилизации которого уже сняли. */
internal fun String.foldToDictionary(): String = lowercase().replace('ё', 'е')

/**
 * Стоит ли запоминать слово: числа, коды и обрывки в личный словарь не попадают.
 */
internal fun String.isLearnable(): Boolean =
    length in MIN_LEARNABLE_LENGTH..MAX_LEARNABLE_LENGTH && all { it.isLetter() || it == '\'' }

private const val MIN_LEARNABLE_LENGTH = 2
private const val MAX_LEARNABLE_LENGTH = 24
