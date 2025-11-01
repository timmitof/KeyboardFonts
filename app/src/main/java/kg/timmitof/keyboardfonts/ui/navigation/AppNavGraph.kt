package kg.timmitof.keyboardfonts.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import kg.timmitof.core.navigation.graphs.SplashGraph
import kg.timmitof.feature_home.presentation.navigation.homeGraph
import kg.timmitof.feature_splash.presentation.navigation.splashGraph

@Composable
internal fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = SplashGraph) {
        homeGraph()
        splashGraph()
    }
}