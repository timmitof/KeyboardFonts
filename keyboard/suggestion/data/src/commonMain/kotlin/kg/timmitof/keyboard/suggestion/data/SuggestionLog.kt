package kg.timmitof.keyboard.suggestion.data

/** Лог загрузки моделей: на Android — Logcat. */
internal expect object SuggestionLog {

    fun debug(tag: String, message: String)

    fun warning(tag: String, message: String)
}
