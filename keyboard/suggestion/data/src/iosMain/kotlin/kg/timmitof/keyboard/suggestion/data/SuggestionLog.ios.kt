package kg.timmitof.keyboard.suggestion.data

internal actual object SuggestionLog {

    actual fun debug(tag: String, message: String) = println("D/$tag: $message")

    actual fun warning(tag: String, message: String) = println("W/$tag: $message")
}
