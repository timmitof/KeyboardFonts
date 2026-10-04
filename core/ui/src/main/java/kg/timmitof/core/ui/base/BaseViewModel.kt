package kg.timmitof.core.ui.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.container

/** База MVI ViewModel; [onBootstrap] вызывается один раз при создании, необработанные ошибки показываются тостом. */
abstract class BaseViewModel<STATE: BaseState, SIDE_EFFECT: BaseSideEffect.UiSideEffect, EVENT: BaseEvent.UiEvent>(
    private val initialState: STATE
) : ViewModel(), ContainerHost<STATE, BaseSideEffect> {

    override val container: Container<STATE, BaseSideEffect> =
        container(
            initialState = initialState,
            buildSettings = {
                exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                    defaultExceptionHandler(throwable)
                }
            },
            onCreate = { onBootstrap() }
        )

    fun onBaseEvent(event: BaseEvent) {
        when (event) {
            BaseEvent.OnBack -> navigateBack()
            is BaseEvent.UiEvent -> Unit
        }
    }

    abstract fun onEvent(event: EVENT)

    protected open suspend fun Syntax<STATE, BaseSideEffect>.onBootstrap() {  }

    open fun defaultExceptionHandler(t: Throwable) = intent {
        showToast(t.message ?: "error occurred")
    }

    protected fun navigateBack() = intent {
        postSideEffect(BaseSideEffect.Navigate(NavigationSideEffect.Back))
    }

    protected fun navigateTo(
        destination: Any,
        popUpTo: Any? = null,
        inclusive: Boolean = false,
        launchSingleTop: Boolean = false,
    ) = intent {
        postSideEffect(
            BaseSideEffect.Navigate(
                NavigationSideEffect.NavigateTo(
                    route = destination,
                    popUpTo = popUpTo,
                    inclusive = inclusive,
                    launchSingleTop = launchSingleTop
                )
            )
        )
    }

    protected fun showToast(message: String) = intent {
        postSideEffect(BaseSideEffect.ShowToast(message))
    }
}