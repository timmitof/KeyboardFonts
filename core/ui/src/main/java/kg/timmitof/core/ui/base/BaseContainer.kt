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

/**
 * Контейнер компонент для UI, с MVI-архитектурой, связывающий [BaseViewModel] с Compose-UI.
 *
 * Основные задачи:
 * 1. **Состояние (State)** — собирает текущее состояние из [viewModel] и передаёт его в [content].
 * 2. **Сайд-эффекты (Side-effects)** — обрабатывает глобальные эффекты (навигация, тосты и пр.)
 *  и проксирует локальные UI-эффекты в зарегистрированный обработчик через [ContainerDSLBuilder.handleSideEffect].
 * 3. **Назад (Back)** — перехватывает системную кнопку «Назад»; если пользователь не задал
 *  собственный обработчик через [ContainerDSLBuilder.onBack], то во [viewModel] отправляется [BaseEvent.OnBack].
 * 4. **UI-обвязка** — предоставляет стандартный [Scaffold] с topBar, bottomBar и FAB.
 *
 * ### Пример использования:
 * ```
 * Container(
 *     viewModel = myViewModel,
 *     topBar = { MyTopBar() },
 * ) { state ->
 *     handleSideEffect { effect ->
 *         when (effect) {
 *             MySideEffect.ShowDialog -> showDialog = true
 *         }
 *     }
 *
 *     onBack { navController.popBackStack() }
 *
 *     MyScreenContent(state)
 * }
 * ```
 *
 * @param STATE тип состояния экрана, расширяющий [BaseState].
 * @param SIDE_EFFECT тип локальных сайд-эффектов, расширяющий [BaseSideEffect.UiSideEffect].
 * @param EVENT тип UI-событий, расширяющий [BaseEvent.UiEvent].
 * @param viewModel экземпляр [BaseViewModel], обеспечивающий MVI-взаимодействие.
 * @param modifier необязательный [Modifier] для настройки внешнего вида контейнера.
 * @param topBar верхняя панель (опционально).
 * @param bottomBar нижняя панель (опционально).
 * @param floatingActionButton плавающая кнопка действия (опционально).
 * @param content тело экрана, получающее текущее состояние [STATE] и DSL-контекст
 * [ContainerDSLBuilder] для регистрации обработчиков и отправки событий.
 *
 * ### Особенности реализации:
 * - Использует `rememberContainerDSL` для сохранения стабильного DSL между рекомпозициями.
 * - Автоматически обрабатывает навигационные эффекты через [BaseSideEffect.Navigate].
 * - Отображает тосты при получении [BaseSideEffect.ShowToast].
 * - Поддерживает безопасное расширение для добавления собственных SideEffect-типов.
 */
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
            is BaseSideEffect.Navigate -> when (sideEffect.navigation) {
                is NavigationSideEffect.Back -> navController.popBackStack()
                is NavigationSideEffect.NavigateTo -> navController.navigate(sideEffect.navigation.route)
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