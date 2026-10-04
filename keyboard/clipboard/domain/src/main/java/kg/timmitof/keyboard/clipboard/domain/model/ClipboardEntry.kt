package kg.timmitof.keyboard.clipboard.domain.model

data class ClipboardEntry(
    val id: Long,
    val text: String,
    val isPinned: Boolean = false,
    val copiedAt: Long = 0L,
)

data class ClipboardBoard(
    val pinned: List<ClipboardEntry> = emptyList(),
    val recent: List<ClipboardEntry> = emptyList(),
) {
    val isEmpty: Boolean get() = pinned.isEmpty() && recent.isEmpty()

    val hasClearable: Boolean get() = recent.isNotEmpty()
}
