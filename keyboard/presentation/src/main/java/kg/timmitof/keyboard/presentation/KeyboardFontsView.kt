package kg.timmitof.keyboard.presentation

import android.content.Context
import android.util.AttributeSet
import android.view.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kg.timmitof.keyboard.presentation.screens.keyboard.states.EnterAction
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardFontsScreen
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardViewModel
import kg.timmitof.keyboard.presentation.theme.KeyboardTheme
import org.orbitmvi.orbit.compose.collectSideEffect

class KeyboardFontsView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val viewModelFactory: ViewModelProvider.Factory,
    private val onKeyboardAction: (KeyboardSideEffect) -> Unit = {},
) : AbstractComposeView(context, attrs = attrs, defStyleAttr = defStyleAttr) {

    private val enterAction = mutableStateOf(EnterAction.RETURN)

    /** Инсеты навигации, измеренные окном IME (см. [KeyboardWindowInsets]). */
    private val windowInsets = mutableStateOf(KeyboardWindowInsets())

    fun updateEnterAction(action: EnterAction) {
        enterAction.value = action
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateWindowInsets()
    }

    /**
     * Инсеты берём у окна напрямую, а не из пришедших сюда: по пути к клавиатуре
     * их успевает поглотить контейнер IME, и до Compose доходят нули.
     * Диспатч детям не трогаем — `super` раздаёт инсеты как обычно.
     */
    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        updateWindowInsets()
        return super.onApplyWindowInsets(insets)
    }

    /**
     * Обычно место под системной панелью резервирует само окно IME — тогда свой отступ не нужен,
     * иначе он удвоится. Считаем его только там, где окно этого не сделало
     * (принудительный edge-to-edge на новых Android): признак — нулевой отступ у корневой view.
     *
     * Пока клавиатура открыта, система рисует внизу свою IME-панель (скрыть клавиатуру, смена
     * клавиатуры). Она выше жестовой полоски, и её высоту `navigationBars` не показывает —
     * реальный размер виден в `systemBars` и `tappableElement`, поэтому берём максимум по типам.
     */
    private fun updateWindowInsets() {
        val reservedByWindow = rootView.paddingBottom > 0
        val rootInsets = ViewCompat.getRootWindowInsets(this)

        windowInsets.value = when {
            reservedByWindow || rootInsets == null -> KeyboardWindowInsets()
            else -> {
                val density = resources.displayMetrics.density
                val systemPanels = INSET_TYPES.map(rootInsets::getInsets)

                KeyboardWindowInsets(
                    left = (systemPanels.maxOf { it.left } / density).dp,
                    right = (systemPanels.maxOf { it.right } / density).dp,
                    bottom = (systemPanels.maxOf { it.bottom } / density).dp
                )
            }
        }
    }

    @Composable
    override fun Content() {
        val viewModel: KeyboardViewModel = viewModel(
            factory = viewModelFactory,
            viewModelStoreOwner = viewModelStoreOwner
        )

        viewModel.collectSideEffect { sideEffect ->
            if (sideEffect is KeyboardSideEffect) {
                onKeyboardAction(sideEffect)
            }
        }

        InputSessionResetEffect(viewModel)

        LaunchedEffect(enterAction.value) {
            viewModel.onEvent(KeyboardEvent.OnEnterActionChange(enterAction.value))
        }

        CompositionLocalProvider(LocalKeyboardWindowInsets provides windowInsets.value) {
            KeyboardTheme {
                KeyboardFontsScreen(viewModel = viewModel)
            }
        }
    }

    private companion object {
        /**
         * Типы инсетов, за которыми может прятаться системная панель под клавиатурой:
         * на разных прошивках её высоту показывает то один, то другой.
         */
        val INSET_TYPES = listOf(
            WindowInsetsCompat.Type.navigationBars(),
            WindowInsetsCompat.Type.systemBars(),
            WindowInsetsCompat.Type.tappableElement()
        )
    }
}

@Composable
private fun InputSessionResetEffect(viewModel: KeyboardViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_PAUSE) {
                viewModel.onEvent(KeyboardEvent.OnInputSessionChange)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
