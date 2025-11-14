package kg.timmitof.keyboard.presentation

import android.content.Context
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import kg.timmitof.keyboard.presentation.model.KeyboardKey
import kg.timmitof.keyboard.presentation.theme.KeyboardTheme

class KeyboardFontsView(context: Context) : AbstractComposeView(context) {
    @Composable
    override fun Content() {
        KeyboardTheme {
            val layout = listOf(
                listOf("q","w","e","r","t","y","u","i","o","p").map { KeyboardKey(it) },
                listOf("a","s","d","f","g","h","j","k","l").map { KeyboardKey(it) },
                listOf("z","x","c","v","b","n","m").map { KeyboardKey(it) },
                listOf(
                    KeyboardKey("123", weight = 1.5f),
                    KeyboardKey("Space", weight = 5f),
                    KeyboardKey("Enter", weight = 1.5f)
                )
            )

            KeyboardFontsScreen(
                modifier = Modifier.fillMaxHeight(0.25f),
                layout = layout,
                onEvent = { }
            )
        }
    }
}