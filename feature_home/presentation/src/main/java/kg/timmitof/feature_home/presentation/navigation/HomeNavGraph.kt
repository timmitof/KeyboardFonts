package kg.timmitof.feature_home.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kg.timmitof.core.navigation.graphs.HomeGraph
import kg.timmitof.feature_home.presentation.screens.HomeScreen

fun NavGraphBuilder.homeGraph() {
   navigation<HomeGraph>(startDestination = HomeGraph.HomeScreen) {
       composable<HomeGraph.HomeScreen> {
           HomeScreen()
       }
   }
}