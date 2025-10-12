package kg.timmitof.core.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

/**
 * DSL-маркер, ограничивающий область видимости билдера [ContainerDSLBuilder]
 * и предотвращающий утечки получателя (receiver leak).
 */
@DslMarker
annotation class ContainerDSL

/**
 * Минималистичный и надёжный DSL, позволяющий экрану регистрировать обработчики
 * **временных (не сохраняемых) UI-задач**:
 *
 * - [handleSideEffect] — обработка одноразовых UI-сайд-эффектов, которые эмитит ViewModel
 *   (например, показ диалога, скролл к элементу, открытие окна «Поделиться»).
 * - [onBack] — переопределение поведения системной кнопки «Назад»;
 *   если обработчик не задан, во ViewModel будет отправлено событие [Event.Back].
 *
 * Usage:
 * ```
 * Container(vm) { state, sendEvent ->
 *   handleSideEffect { effect -> /* обработка SideEffect */ }
 *   onBack { /* своя логика при нажатии Назад */ }
 *   // ...
 * }
 * ```
 */
@ContainerDSL
class ContainerDSLBuilder<SIDE_EFFECT: BaseSideEffect.UiSideEffect, EVENT: BaseEvent.UiEvent>(
    private var sendEventCallback: SendEvent<EVENT>
) {
    private var onSideEffectCallback: ((SIDE_EFFECT) -> Unit)? = null

    /** Необязательное переопределение обработки системной кнопки «назад» на данном экране. */
    private var onBackPressed: (() -> Unit)? = null

    /**
     * Вызвать зарегистрированный обработчик сайд-эффекта [SIDE_EFFECT].
     */
    internal fun notifySideEffectCallback(sideEffect: SIDE_EFFECT) = onSideEffectCallback?.invoke(sideEffect)

    /**
     * Вызвать зарегистрированный обработчик системной кнопки «Назад».
     */
    internal fun notifyBackPress() = onBackPressed?.invoke()

    /**
     * Зарегистрировать обработчик для разовых UI-сайд-эффектов.
     * Обработчик вызывается для эффектов типа [SIDE_EFFECT].
     *
     * Типичные случаи: показать снекбар/диалог, запустить тактильный отклик, прокрутить список, открыть bottom sheet и т. п.
     */
    fun handleSideEffect(block: (SIDE_EFFECT) -> Unit) {
        onSideEffectCallback = block
    }

    /**
     * Переопределить поведение «назад» для этого экрана.
     *
     * Если не задано, во ViewModel будет отправлен [BaseEvent.OnBack] (пусть VM решает, что делать).
     */
    fun onBack(block: () -> Unit) {
        onBackPressed = block
    }

    /** Отправить MVI-событие во ViewModel. */
    fun sendEvent(event: EVENT) {
        sendEventCallback(event)
    }
}

/**
 * Создаёт и запоминает экземпляр [ContainerDSLBuilder] для экрана.
 *
 * @param sendEvent Колбэк для отправки событий [EVENT] во ViewModel.
 * @return Экземпляр [ContainerDSLBuilder], запоминаемый через [remember].
 */
@Composable
fun <SIDE_EFFECT: BaseSideEffect.UiSideEffect, EVENT: BaseEvent.UiEvent> rememberContainerDSL(sendEvent: (EVENT) -> Unit) = remember {
    ContainerDSLBuilder<SIDE_EFFECT, EVENT>(sendEventCallback = sendEvent)
}

/**
 * Стабильный тип функции для отправки событий из UI в ViewModel.
 *
 * Почему обёртка, а не «сырая» лямбда?
 * - Даёт понятный, легко обнаруживаемый API в месте вызова: `sendEvent(MyUiEvent.Clicked)`
 * - Помечен как [Stable], чтобы сохранить аккуратные рекомпозиции при хранении/remember в UI-дереве.
 *
 * Пример:
 * ```
 * Container(vm) { state, sendEvent ->
 *   Button(onClick = { sendEvent(MyUiEvent.Submit) }) { Text("Submit") }
 * }
 * ```
 */
fun interface SendEvent<E> {
    /** Отправить UI-событие в ViewModel экрана. */
    operator fun invoke(event: E)
}
