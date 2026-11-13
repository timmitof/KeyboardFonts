package kg.timmitof.keyboard.clipboard.domain.model

/**
 * Текст, который только что скопировали в системный буфер.
 *
 * @param copiedAt время копирования по часам системы: по нему одну и ту же копию не обрабатывают дважды.
 */
data class FreshClip(
    val text: String,
    val copiedAt: Long,
)
