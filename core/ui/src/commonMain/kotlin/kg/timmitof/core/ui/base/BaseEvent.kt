package kg.timmitof.core.ui.base

sealed class BaseEvent {
    data object OnBack : BaseEvent()

    abstract class UiEvent : BaseEvent()
}