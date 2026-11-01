package kg.timmitof.shared

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import kg.timmitof.core.navigation.LocalNavController
import kg.timmitof.core.navigation.NavControllerProvider
import kg.timmitof.core.navigation.graphs.SplashGraph
import kg.timmitof.core.ui.theme.KeyboardFontsTheme
import kg.timmitof.feature_home.presentation.navigation.homeGraph
import kg.timmitof.feature_settings.presentation.navigation.settingsGraph
import kg.timmitof.feature_splash.presentation.navigation.splashGraph

/** Приложение целиком — тема, навигация и экраны фич; одно для Android и iOS. */
@Composable
fun App() {
    KeyboardFontsTheme {
        NavControllerProvider {
            NavHost(navController = LocalNavController.current, startDestination = SplashGraph) {
                settingsGraph()
                homeGraph()
                splashGraph()
            }
        }
    }
}
