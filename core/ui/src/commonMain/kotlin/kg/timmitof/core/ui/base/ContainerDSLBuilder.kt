package kg.timmitof.core.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

@DslMarker
annotation class ContainerDSL

/** DSL экрана: обработчики UI-сайд-эффектов и «Назад»; без onBack во ViewModel уходит [BaseEvent.OnBack]. */
@ContainerDSL
class ContainerDSLBuilder<SIDE_EFFECT: BaseSideEffect.UiSideEffect, EVENT: BaseEvent.UiEvent>(
    private var sendEventCallback: SendEvent<EVENT>
) {
    private var onSideEffectCallback: ((SIDE_EFFECT) -> Unit)? = null

    private var onBackPressed: (() -> Unit)? = null

    internal fun notifySideEffectCallback(sideEffect: SIDE_EFFECT) = onSideEffectCallback?.invoke(sideEffect)

    internal fun notifyBackPress() = onBackPressed?.invoke()

    fun handleSideEffect(block: (SIDE_EFFECT) -> Unit) {
        onSideEffectCallback = block
    }

    fun onBack(block: () -> Unit) {
        onBackPressed = block
    }

    fun sendEvent(event: EVENT) {
        sendEventCallback(event)
    }

    fun sendEvent(block: () -> EVENT) {
        sendEventCallback(block())
    }
}

@Composable
fun <SIDE_EFFECT: BaseSideEffect.UiSideEffect, EVENT: BaseEvent.UiEvent> rememberContainerDSL(sendEvent: (EVENT) -> Unit) = remember {
    ContainerDSLBuilder<SIDE_EFFECT, EVENT>(sendEventCallback = sendEvent)
}

fun interface SendEvent<E> {
    operator fun invoke(event: E)
}
