package kg.timmitof.keyboard.clipboard.domain.model

/**
 * Запись буфера обмена.
 *
 * @property id ключ строки в базе.
 * @property isPinned закреплённые.
 * @property copiedAt момент копирования.
 */
data class ClipboardEntry(
    val id: Long,
    val text: String,
    val isPinned: Boolean = false,
    val copiedAt: Long = 0L,
)

/** Буфер, разложенный по секциям листа. */
data class ClipboardBoard(
    val pinned: List<ClipboardEntry> = emptyList(),
    val recent: List<ClipboardEntry> = emptyList(),
) {
    val isEmpty: Boolean get() = pinned.isEmpty() && recent.isEmpty()

    /** Есть ли что чистить корзиной: закреплённые она не трогает. */
    val hasClearable: Boolean get() = recent.isNotEmpty()
}
