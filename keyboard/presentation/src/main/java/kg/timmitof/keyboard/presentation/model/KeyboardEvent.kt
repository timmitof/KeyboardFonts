package kg.timmitof.keyboard.presentation.model

sealed interface KeyboardEvent {
    data class OnKeyClick(val key: KeyboardKey) : KeyboardEvent
}