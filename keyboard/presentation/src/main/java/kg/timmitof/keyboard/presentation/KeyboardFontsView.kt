package kg.timmitof.keyboard.presentation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AbstractComposeView
import kg.timmitof.keyboard.presentation.model.KeyboardKey
import kg.timmitof.keyboard.presentation.theme.KeyboardTheme

class KeyboardFontsView(context: Context) : AbstractComposeView(context) {
    @Composable
    override fun Content() {
        KeyboardTheme {
            val layout = listOf(
                listOf("Q","W","E","R","T","Y","U","I","O","P").map { KeyboardKey(it) },
                listOf("A","S","D","F","G","H","J","K","L").map { KeyboardKey(it) },
                listOf("Z","X","C","V","B","N","M").map { KeyboardKey(it) },
                listOf(KeyboardKey("Space", weight = 5f))
            )

            KeyboardFontsScreen(
                layout = layout,
                onEvent = {  }
            )
        }
    }
}