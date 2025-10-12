package kg.timmitof.core.ui.base

/**
 * Базовый тип событий (Event) в MVI-архитектуре.
 *
 * Используется для описания действий пользователя или системных событий,
 * которые обрабатывает [ViewModel].
 *
 * - [OnBack] — стандартное событие для обработки нажатия «Назад», обрабатывается в базовом слое.
 * - [UiEvent] — базовый класс для пользовательских событий экрана.
 *
 * Пример:
 * ```
 * sealed class MyEvent : BaseEvent.UiEvent() {
 *     data object SomeEvent : MyEvent()
 * }
 * ```
 */
sealed class BaseEvent {
    data object OnBack : BaseEvent()

    abstract class UiEvent : BaseEvent()
}