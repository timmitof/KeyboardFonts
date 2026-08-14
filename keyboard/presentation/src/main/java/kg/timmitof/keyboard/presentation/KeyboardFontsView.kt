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
import androidx.compose.ui.platform.AbstractComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.suggestion.domain.model.TextContext
import kg.timmitof.keyboard.presentation.insets.KeyboardInsetsTracker
import kg.timmitof.keyboard.presentation.insets.LocalKeyboardInsets
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardFieldContext
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardFontsScreen
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardViewModel
import kg.timmitof.keyboard.presentation.theme.KeyboardTheme
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

    /** Сервис отдаёт сюда разобранный `EditorInfo` при каждой смене поля ввода. */
    fun updateFieldContext(context: KeyboardFieldContext) {
        fieldContext.value = context
    }

    /** Сервис отдаёт сюда текст вокруг курсора при каждой правке поля — вход Т9. */
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

        LaunchedEffect(fieldContext.value) {
            viewModel.onEvent(KeyboardEvent.OnFieldContextChange(fieldContext.value))
        }

        LaunchedEffect(textContext.value) {
            viewModel.onEvent(KeyboardEvent.OnTextContextChange(textContext.value))
        }

        val insets by insetsTracker.insets

        CompositionLocalProvider(LocalKeyboardInsets provides insets) {
            KeyboardTheme(darkTheme = viewModel.isDarkTheme()) {
                KeyboardFontsScreen(viewModel = viewModel)
            }
        }
    }
}

/**
 * Тема клавиатуры: следовать системе или держать выбранную в настройках.
 *
 * Состояние читается через `derivedStateOf`, чтобы поток нажатий не перекрашивал
 * клавиатуру: тему меняют раз в жизни, а состояние — на каждую букву.
 */
@Composable
private fun KeyboardViewModel.isDarkTheme(): Boolean {
    val isSystemDark = isSystemInDarkTheme()
    val state = collectAsState()

    val isDark by remember(isSystemDark) {
        derivedStateOf {
            when (state.value.settings.theme) {
                KeyboardThemeMode.AUTO -> isSystemDark
                KeyboardThemeMode.LIGHT -> false
                KeyboardThemeMode.DARK -> true
            }
        }
    }

    return isDark
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
