package kg.timmitof.keyboardfonts

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import kg.timmitof.keyboardfonts.ui.navigation.AppNavHost
import kg.timmitof.core.navigation.LocalNavController
import kg.timmitof.core.navigation.NavControllerProvider
import kg.timmitof.core.ui.locale.AppLocale
import kg.timmitof.core.ui.theme.KeyboardFontsTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KeyboardFontsTheme {
                NavControllerProvider {
                    val navController = LocalNavController.current
                    AppNavHost(navController = navController)
                }
            }
        }
    }
}