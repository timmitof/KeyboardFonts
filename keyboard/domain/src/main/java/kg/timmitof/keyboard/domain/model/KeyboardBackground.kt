package kg.timmitof.keyboard.domain.model

sealed interface KeyboardBackground {

    /** Фон темы клавиатуры. */
    data object None : KeyboardBackground

    data class Solid(val argb: Long) : KeyboardBackground

    data class Pattern(val pattern: BackgroundPattern) : KeyboardBackground

    /** [path] — обрезанная копия во внутренней папке приложения; [tone] — средний цвет, под него подбираются клавиши. */
    data class Photo(val path: String, val tone: Long) : KeyboardBackground

    companion object {
        val Default: KeyboardBackground = None

        fun of(key: String?): KeyboardBackground {
            val (type, value) = key?.split(SEPARATOR, limit = 2)?.takeIf { it.size == 2 }
                ?: return Default

            return when (type) {
                SOLID -> value.toLongOrNull(16)?.let(::Solid)
                PATTERN -> BackgroundPattern.entries.firstOrNull { it.key == value }?.let(::Pattern)
                PHOTO -> value.split(SEPARATOR, limit = 2).takeIf { it.size == 2 }?.let { (tone, path) ->
                    tone.toLongOrNull(16)?.let { Photo(path, it) }
                }
                else -> null
            } ?: Default
        }
    }
}

fun KeyboardBackground.toKey(): String? = when (this) {
    KeyboardBackground.None -> null
    is KeyboardBackground.Solid -> "$SOLID$SEPARATOR${argb.toString(16)}"
    is KeyboardBackground.Pattern -> "$PATTERN$SEPARATOR${pattern.key}"
    is KeyboardBackground.Photo -> "$PHOTO$SEPARATOR${tone.toString(16)}$SEPARATOR$path"
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

/** Доли от сторон исходной картинки (0..1): какой кусок фото пойдёт на фон. */
data class PhotoCrop(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)
