package kg.timmitof.keyboard.presentation

import android.content.Context
import android.util.AttributeSet
import android.view.WindowInsets
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.AbstractComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kg.timmitof.keyboard.suggestion.domain.model.TextContext
import kg.timmitof.keyboard.presentation.insets.KeyboardInsetsTracker
import kg.timmitof.keyboard.presentation.insets.LocalKeyboardInsets
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardFieldContext
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardFontsScreen
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardViewModel
import kg.timmitof.keyboard.presentation.theme.KeyboardAppearance
import kg.timmitof.keyboard.presentation.theme.KeyboardTheme
import kg.timmitof.keyboard.presentation.theme.appearance
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

class KeyboardFontsView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val viewModelFactory: ViewModelProvider.Factory,
    private val onKeyboardAction: (KeyboardSideEffect) -> Unit = {},
) : AbstractComposeView(context, attrs = attrs, defStyleAttr = defStyleAttr) {

    private val fieldContext = mutableStateOf(KeyboardFieldContext())

    private val textContext = mutableStateOf(TextContext())

    private val insetsTracker = KeyboardInsetsTracker(this)

    fun updateFieldContext(context: KeyboardFieldContext) {
        fieldContext.value = context
    }

    fun updateTextContext(context: TextContext) {
        textContext.value = context
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        insetsTracker.startTracking()
    }

    override fun onDetachedFromWindow() {
        insetsTracker.stopTracking()
        super.onDetachedFromWindow()
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        insetsTracker.measure()
        return super.onApplyWindowInsets(insets)
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

        // Снимки поля читаются вне композиции: синхронизация текста не рекомпозирует корень и не ждёт кадра.
        LaunchedEffect(viewModel) {
            snapshotFlow { fieldContext.value }
                .collect { viewModel.onEvent(KeyboardEvent.OnFieldContextChange(it)) }
        }

        LaunchedEffect(viewModel) {
            snapshotFlow { textContext.value }
                .collect { viewModel.onEvent(KeyboardEvent.OnTextContextChange(it)) }
        }

        val insets by insetsTracker.insets

        CompositionLocalProvider(LocalKeyboardInsets provides insets) {
            KeyboardTheme(appearance = viewModel.appearance()) {
                KeyboardFontsScreen(viewModel = viewModel)
            }
        }
    }
}

/** Состояние читается через `derivedStateOf`, чтобы поток нажатий не перекрашивал клавиатуру. */
@Composable
private fun KeyboardViewModel.appearance(): KeyboardAppearance {
    val isSystemDark = isSystemInDarkTheme()
    val state = collectAsState()

    val appearance by remember(isSystemDark) {
        derivedStateOf { state.value.settings.appearance(isSystemDark) }
    }

    return appearance
}

@Composable
private fun InputSessionResetEffect(viewModel: KeyboardViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            // Сброс — только на старте ввода: на паузе он дублировал бы ресет следующего ON_RESUME.
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.onEvent(KeyboardEvent.OnInputSessionChange)
                Lifecycle.Event.ON_PAUSE -> viewModel.onEvent(KeyboardEvent.OnInputSessionFinish)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
