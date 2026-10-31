package kg.timmitof.keyboard.data

/**
 * Бинарный ассет только для чтения, по возможности отображённый в память: большие индексы не копируются в кучу.
 * Числа — big-endian. Чтения абсолютные, поэтому безопасны из нескольких потоков.
 */
expect class MappedBytes {

    val size: Int

    fun byteAt(index: Int): Byte

    fun intAt(index: Int): Int

    fun charAt(index: Int): Char
}
