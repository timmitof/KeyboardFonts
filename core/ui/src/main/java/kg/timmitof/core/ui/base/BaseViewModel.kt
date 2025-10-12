package kg.timmitof.core.ui.base

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.container

/**
 * Базовая ViewModel для MVI-архитектуры, работающая с [BaseState], [BaseSideEffect] и [BaseEvent].
 *
 * Отвечает за:
 * 1. Хранение и управление состоянием экрана [STATE].
 * 2. Генерацию одноразовых сайд-эффектов [SIDE_EFFECT] через `postSideEffect`.
 * 3. Обработку UI-событий [EVENT] и глобальных событий [BaseEvent.OnBack].
 *
 * ### Особенности реализации:
 * - Использует [Container] для хранения состояния и сайд-эффектов.
 * - Метод [onBootstrap] вызывается один раз при создании ViewModel для начальной настройки.
 * - `defaultExceptionHandler` обрабатывает непойманные ошибки и по умолчанию показывает тост.
 * - Методы `navigateBack`, `navigateTo`, `showToast` удобны для отправки стандартных сайд-эффектов.
 *
 * ### Пример использования:
 * ```
 * class MyViewModel : BaseViewModel<MyState, MySideEffect, MyEvent>(
 *     initialState = MyState()
 * ) {
 *     override fun onEvent(event: MyEvent) {
 *         when (event) {
 *             is MyEvent.SomeEvent -> /* обработка ивента */
 *         }
 *     }
 * }
 * ```
 *
 * @param STATE тип состояния экрана, расширяющий [BaseState].
 * @param SIDE_EFFECT тип локальных UI-сайд-эффектов экрана, расширяющий [BaseSideEffect.UiSideEffect].
 * @param EVENT тип пользовательских событий экрана, расширяющий [BaseEvent.UiEvent].
 * @param initialState начальное состояние экрана.
 */
abstract class BaseViewModel<STATE: BaseState, SIDE_EFFECT: BaseSideEffect.UiSideEffect, EVENT: BaseEvent.UiEvent>(
    private val initialState: STATE
) : ViewModel(), ContainerHost<STATE, BaseSideEffect> {

    private val onCreate: suspend Syntax<STATE, BaseSideEffect>.() -> Unit = {
        runCatching {
            intent { onBootstrap() }
        }.onFailure(::defaultExceptionHandler)
    }

    override val container: Container<STATE, BaseSideEffect> =
        container(
            initialState = initialState,
            onCreate = onCreate
        )

    fun onBaseEvent(event: BaseEvent) {
        when (event) {
            BaseEvent.OnBack -> navigateBack()
            is BaseEvent.UiEvent -> (event as? EVENT)?.let { onEvent(it) }
        }
    }

    abstract fun onEvent(event: BaseEvent)

    protected open suspend fun Syntax<STATE, BaseSideEffect>.onBootstrap() {  }

    open fun defaultExceptionHandler(t: Throwable) = intent {
        showToast(t.message ?: "error occurred")
    }

    protected fun navigateBack() = intent {
        postSideEffect(BaseSideEffect.Navigate(NavigationSideEffect.Back))
    }

    protected fun navigateTo(destination: Any) = intent {
        postSideEffect(BaseSideEffect.Navigate(NavigationSideEffect.NavigateTo(destination)))
    }

    protected fun showToast(message: String) = intent {
        postSideEffect(BaseSideEffect.ShowToast(message))
    }
}