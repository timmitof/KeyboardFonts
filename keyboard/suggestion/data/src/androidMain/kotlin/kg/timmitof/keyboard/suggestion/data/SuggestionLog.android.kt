package kg.timmitof.keyboard.suggestion.data

import android.util.Log

internal actual object SuggestionLog {

    actual fun debug(tag: String, message: String) {
        Log.d(tag, message)
    }

    actual fun warning(tag: String, message: String) {
        Log.w(tag, message)
    }
}
