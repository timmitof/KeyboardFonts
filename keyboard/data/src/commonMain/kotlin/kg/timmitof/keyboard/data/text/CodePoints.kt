package kg.timmitof.keyboard.data.text

// Кодовые точки Unicode для общего кода: в stdlib они есть только на JVM.

private const val MIN_SUPPLEMENTARY = 0x10000

/** Кодовая точка с позиции [index]; одиночный суррогат возвращается как есть. */
internal fun CharSequence.codePointAtIndex(index: Int): Int {
    val high = this[index]
    if (high.isHighSurrogate() && index + 1 < length) {
        val low = this[index + 1]
        if (low.isLowSurrogate()) {
            return ((high.code - 0xD800) shl 10) + (low.code - 0xDC00) + MIN_SUPPLEMENTARY
        }
    }
    return high.code
}

/** Сколько UTF-16 символов занимает кодовая точка. */
internal fun charCount(codePoint: Int): Int = if (codePoint >= MIN_SUPPLEMENTARY) 2 else 1

internal fun codePointToString(codePoint: Int): String {
    if (codePoint < MIN_SUPPLEMENTARY) return codePoint.toChar().toString()

    val offset = codePoint - MIN_SUPPLEMENTARY
    return charArrayOf(((offset ushr 10) + 0xD800).toChar(), ((offset and 0x3FF) + 0xDC00).toChar()).concatToString()
}

internal fun CharSequence.codePointList(): List<Int> {
    val result = ArrayList<Int>(length)
    var index = 0
    while (index < length) {
        val codePoint = codePointAtIndex(index)
        result += codePoint
        index += charCount(codePoint)
    }
    return result
}
