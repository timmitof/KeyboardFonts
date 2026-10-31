package kg.timmitof.keyboard.suggestion.data

/**
 * Конец содержимого строки `[lineStart, lineEnd)` без завершающего `\r`.
 * Ассеты могут прийти с CRLF (git на Windows), а разбор идёт по индексам без `split`.
 */
internal fun String.lineContentEnd(lineStart: Int, lineEnd: Int): Int =
    if (lineEnd > lineStart && this[lineEnd - 1] == '\r') lineEnd - 1 else lineEnd
