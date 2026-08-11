package kg.timmitof.core.ui.base

/**
 * Базовый тип сайд-эффектов (SideEffect) в MVI-архитектуре.
 *
 * ### Виды встроенных эффектов:
 * - [ShowToast] — показать короткое сообщение пользователю.
 * - [Navigate] — выполнить навигационное действие через [NavigationSideEffect].
 * - [UiSideEffect] — тип для локальных UI-эффектов конкретного экрана.
 *
 * ### Пример:
 * ```
 * sealed class MySideEffect : BaseSideEffect.UiSideEffect() {
 *     data object SomeEffect : MySideEffect()
 *     data class SomeEffect(val pattern: LongArray) : MySideEffect()
 * }
 * ```
 */
sealed class BaseSideEffect {

    data class ShowToast(val message: String) : BaseSideEffect()

    data class Navigate(val navigation: NavigationSideEffect) : BaseSideEffect()

    abstract class UiSideEffect : BaseSideEffect()
}

/**
 * Модель навигационного сайд-эффекта.
 *
 * Используется внутри [BaseSideEffect.Navigate] для описания направлений перехода.
 *
 * ### Варианты:
 * - [NavigateTo] — перейти по маршруту.
 * - [Back] — вернуться на предыдущий экран.
 */
sealed class NavigationSideEffect {

    data class NavigateTo(
        val route: Any,
        val popUpTo: Any? = null,
        val inclusive: Boolean = false,
        val launchSingleTop: Boolean = false,
    ) : NavigationSideEffect()

    data object Back : NavigationSideEffect()
}