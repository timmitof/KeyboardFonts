package kg.timmitof.core.ui.base

sealed class BaseSideEffect {

    data class ShowToast(val message: String) : BaseSideEffect()

    data class Navigate(val navigation: NavigationSideEffect) : BaseSideEffect()

    abstract class UiSideEffect : BaseSideEffect()
}

sealed class NavigationSideEffect {

    data class NavigateTo(
        val route: Any,
        val popUpTo: Any? = null,
        val inclusive: Boolean = false,
        val launchSingleTop: Boolean = false,
    ) : NavigationSideEffect()

    data object Back : NavigationSideEffect()
}