package kg.timmitof.keyboard.domain.model

sealed class LongPressAction {
    data class Symbols(val symbols: List<String>) : LongPressAction()
    data object Microphone : LongPressAction()
}