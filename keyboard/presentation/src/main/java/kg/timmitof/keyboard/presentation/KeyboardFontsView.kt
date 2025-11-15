package kg.timmitof.keyboard.presentation

import android.content.Context
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import kg.timmitof.keyboard.domain.model.KeyboardLayout
import kg.timmitof.keyboard.presentation.theme.KeyboardTheme

class KeyboardFontsView(context: Context) : AbstractComposeView(context) {
    @Composable
    override fun Content() {
        KeyboardTheme {
            KeyboardFontsScreen(
                modifier = Modifier.fillMaxHeight(0.25f),
                layout = KeyboardLayout("English", emptyList()),
                onEvent = { }
            )
        }
    }
}