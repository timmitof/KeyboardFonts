package kg.timmitof.keyboard.presentation

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.AbstractComposeView
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

    fun updateEnterAction(action: EnterAction) {
        enterAction.value = action
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

        KeyboardTheme {
            KeyboardFontsScreen(viewModel = viewModel)
        }
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
