package kg.timmitof.keyboard.data

import java.nio.ByteBuffer
import java.nio.ByteOrder

actual class MappedBytes(buffer: ByteBuffer) {

    // Срез: индекс 0 — начало ассета, даже если позиция буфера была не в нуле.
    private val buffer: ByteBuffer = buffer.slice().order(ByteOrder.BIG_ENDIAN)

    actual val size: Int = this.buffer.limit()

    actual fun byteAt(index: Int): Byte = buffer.get(index)

    actual fun intAt(index: Int): Int = buffer.getInt(index)

    actual fun charAt(index: Int): Char = buffer.getChar(index)
}
