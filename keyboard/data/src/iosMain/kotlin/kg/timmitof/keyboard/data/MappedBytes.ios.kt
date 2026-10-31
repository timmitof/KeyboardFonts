package kg.timmitof.keyboard.data

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import platform.Foundation.NSData

/** Держит [data]: пока объект жив, отображение файла не освобождается. */
@OptIn(ExperimentalForeignApi::class)
actual class MappedBytes(private val data: NSData) {

    private val bytes: CPointer<ByteVar>? = data.bytes?.reinterpret()

    actual val size: Int = data.length.toInt()

    actual fun byteAt(index: Int): Byte = byte(checked(index, Byte.SIZE_BYTES))

    actual fun intAt(index: Int): Int {
        val start = checked(index, Int.SIZE_BYTES)
        return (unsigned(start) shl 24) or (unsigned(start + 1) shl 16) or
            (unsigned(start + 2) shl 8) or unsigned(start + 3)
    }

    actual fun charAt(index: Int): Char {
        val start = checked(index, Char.SIZE_BYTES)
        return ((unsigned(start) shl 8) or unsigned(start + 1)).toChar()
    }

    private fun byte(index: Int): Byte = requireNotNull(bytes)[index]

    private fun unsigned(index: Int): Int = byte(index).toInt() and 0xFF

    // Чтение за границей — это чужая память, а не исключение, как у ByteBuffer: проверяем сами.
    private fun checked(index: Int, width: Int): Int {
        if (index < 0 || index > size - width) throw IndexOutOfBoundsException("$index + $width > $size")
        return index
    }
}
