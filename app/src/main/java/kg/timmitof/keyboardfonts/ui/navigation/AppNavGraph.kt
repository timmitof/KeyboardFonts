package kg.timmitof.keyboardfonts.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import kg.timmitof.core.navigation.graphs.HomeGraph
import kg.timmitof.feature_home.presentation.navigation.homeGraph

@Composable
internal fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = HomeGraph) {
        homeGraph(navController)
    }
}