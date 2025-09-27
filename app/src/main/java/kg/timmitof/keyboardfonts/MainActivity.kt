package kg.timmitof.keyboardfonts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kg.timmitof.keyboardfonts.ui.AppNavHost
import kg.timmitof.ui.theme.KeyboardFontsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KeyboardFontsTheme {
                AppNavHost()
            }
        }
    }
}