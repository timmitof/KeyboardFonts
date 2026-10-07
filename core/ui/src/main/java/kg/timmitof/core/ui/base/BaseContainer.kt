package kg.timmitof.core.ui.base

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kg.timmitof.core.navigation.LocalNavController
import kg.timmitof.core.ui.showToast
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/** Экран MVI: связывает [viewModel] с Compose (state, side effects, «Назад»); без своего onBack шлёт [BaseEvent.OnBack]. */
@Composable
fun <STATE: BaseState, SIDE_EFFECT: BaseSideEffect.UiSideEffect, EVENT: BaseEvent.UiEvent> Container(
    viewModel: BaseViewModel<STATE, SIDE_EFFECT, EVENT>,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable ContainerDSLBuilder<SIDE_EFFECT, EVENT>.(state: State<STATE>, innerPadding: PaddingValues) -> Unit,
) {
    val navController = LocalNavController.current
    val context = LocalContext.current

    val state = viewModel.collectAsState()
    val containerDsl = rememberContainerDSL<SIDE_EFFECT, EVENT>(sendEvent = viewModel::onEvent)

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is BaseSideEffect.Navigate -> when (val navigation = sideEffect.navigation) {
                is NavigationSideEffect.Back -> navController.popBackStack()
                is NavigationSideEffect.NavigateTo -> navController.navigate(navigation.route) {
                    navigation.popUpTo?.let { target ->
                        popUpTo(target) { inclusive = navigation.inclusive }
                    }
                    launchSingleTop = navigation.launchSingleTop
                }
            }
            is BaseSideEffect.ShowToast -> context.showToast(sideEffect.message)
            is BaseSideEffect.UiSideEffect -> {
                (sideEffect as? SIDE_EFFECT)?.let { containerDsl.notifySideEffectCallback(it) }
            }
        }
    }

    BackHandler(
        onBack = {
            containerDsl.notifyBackPress() ?: viewModel.onBaseEvent(BaseEvent.OnBack)
        }
    )

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        content = { innerPadding ->
            containerDsl.content(state, innerPadding)
        }
    )
}