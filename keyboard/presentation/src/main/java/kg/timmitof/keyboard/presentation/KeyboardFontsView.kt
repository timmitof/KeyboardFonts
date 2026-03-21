package kg.timmitof.keyboard.presentation

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AbstractComposeView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardFontsScreen
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardViewModel
import kg.timmitof.keyboard.presentation.theme.KeyboardTheme
import org.orbitmvi.orbit.compose.collectAsState

class KeyboardFontsView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val viewModelFactory: ViewModelProvider.Factory,
) : AbstractComposeView(context, attrs = attrs, defStyleAttr = defStyleAttr) {

    @Composable
    override fun Content() {
        val viewModel: KeyboardViewModel = viewModel(
            factory = viewModelFactory,
            viewModelStoreOwner = viewModelStoreOwner
        )

        val state = viewModel.collectAsState()

        KeyboardTheme {
            KeyboardFontsScreen(
                state = state,
                onEvent = { }
            )
        }
    }
}