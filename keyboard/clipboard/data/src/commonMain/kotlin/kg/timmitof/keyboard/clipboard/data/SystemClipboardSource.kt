package kg.timmitof.keyboard.clipboard.data

/** Текст системного буфера обмена для истории клавиатуры. */
interface SystemClipboardSource {

    /** `null` — буфер пуст, в нём не текст или система пометила его как секретный. */
    fun read(): String?
}

/** Текст без пробелов по краям, если он годится в историю: не пустой и не слишком длинный. */
internal fun String.toClipText(): String? =
    trim().takeIf { it.isNotEmpty() && it.length <= MAX_TEXT_LENGTH }

private const val MAX_TEXT_LENGTH = 2_000
