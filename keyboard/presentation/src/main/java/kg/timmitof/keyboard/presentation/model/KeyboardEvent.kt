package kg.timmitof.keyboard.presentation.model

import kg.timmitof.keyboard.domain.model.KeyboardKey

sealed interface KeyboardEvent {
    data class OnKeyClick(val key: KeyboardKey) : KeyboardEvent
}