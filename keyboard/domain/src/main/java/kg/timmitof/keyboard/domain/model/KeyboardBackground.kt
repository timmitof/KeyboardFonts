package kg.timmitof.keyboard.domain.model

sealed interface KeyboardBackground {

    /** Фон темы клавиатуры. */
    data object None : KeyboardBackground

    data class Solid(val argb: Long) : KeyboardBackground

    data class Pattern(val pattern: BackgroundPattern) : KeyboardBackground

    data class Photo(val photo: BackgroundPhoto) : KeyboardBackground

    companion object {
        val Default: KeyboardBackground = None

        /** Нужна ли для этого ключа база с фото: для остальных фонов Room открывать незачем. */
        fun isPhotoKey(key: String?): Boolean = key?.startsWith("$PHOTO$SEPARATOR") == true

        /** Фото хранится ссылкой на id: сами фото со своим кадром живут в базе, [photo] достаёт их оттуда. */
        fun of(key: String?, photo: (id: Long) -> BackgroundPhoto? = { null }): KeyboardBackground {
            val (type, value) = key?.split(SEPARATOR, limit = 2)?.takeIf { it.size == 2 }
                ?: return Default

            return when (type) {
                SOLID -> value.toLongOrNull(16)?.let(::Solid)
                PATTERN -> BackgroundPattern.entries.firstOrNull { it.key == value }?.let(::Pattern)
                PHOTO -> value.toLongOrNull()?.let(photo)?.let(::Photo)
                else -> null
            } ?: Default
        }
    }
}

fun KeyboardBackground.toKey(): String? = when (this) {
    KeyboardBackground.None -> null
    is KeyboardBackground.Solid -> "$SOLID$SEPARATOR${argb.toString(16)}"
    is KeyboardBackground.Pattern -> "$PATTERN$SEPARATOR${pattern.key}"
    is KeyboardBackground.Photo -> "$PHOTO$SEPARATOR${photo.id}"
}

private const val SEPARATOR = ":"
private const val SOLID = "solid"
private const val PATTERN = "pattern"
private const val PHOTO = "photo"

enum class BackgroundPattern(val key: String) {
    MINT("mint"),
    STRIPES("stripes"),
    BUBBLES("bubbles"),
    GRID("grid"),
    WAVES("waves"),
    NIGHT("night"),
}

/** Фото из галереи: [path] — полная копия в папке приложения, [tone] — средний цвет для палитры клавиш. */
data class BackgroundPhoto(
    val id: Long,
    val path: String,
    val tone: Long,
    val crop: PhotoCrop = PhotoCrop.Full,
)

/** Доли от сторон исходной картинки (0..1): какой кусок фото виден на фоне. Файл при этом не обрезается. */
data class PhotoCrop(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    companion object {
        val Full = PhotoCrop(0f, 0f, 1f, 1f)
    }
}
