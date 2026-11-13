package kg.timmitof.keyboard.translate.data.text

/**
 * Готовые переводы предложений (LRU). Пока набираешь, меняется только последнее предложение —
 * остальные берутся отсюда, и длинное сообщение не переводится заново на каждую паузу.
 * Потокобезопасна: переводы приходят из разных корутин.
 */
internal class TranslationMemory(private val capacity: Int) {

    data class Key(
        val sourceCode: String,
        val targetCode: String,
        val text: String,
    )

    private val entries = object : LinkedHashMap<Key, String>(capacity, LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Key, String>): Boolean = size > capacity
    }

    @Synchronized
    operator fun get(key: Key): String? = entries[key]

    @Synchronized
    operator fun set(key: Key, translation: String) {
        entries[key] = translation
    }

    private companion object {
        const val LOAD_FACTOR = 0.75f
    }
}
